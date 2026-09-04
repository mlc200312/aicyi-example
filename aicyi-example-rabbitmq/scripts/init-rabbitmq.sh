#!/usr/bin/env bash
# =============================================================================
# RabbitMQ 环境初始化脚本（幂等，可重复执行）
#
# 拓扑职责边界（生产约定）：
#   应用侧 nacos/aicyi-example-rabbitmq.yml 所有绑定均声明 declare-exchange: false /
#   bind-queue: false，binder 不建交换机、不建绑定；交换机 / 队列 / 绑定 / 死信路由
#   全部由本脚本（或 DBA 工单）预建。
#
#   ⚠️ 但 binder 仍会「幂等声明队列」（消费者属性里有 declare-exchange / bind-queue，
#      却没有 declare-queue 开关，而 queue-declaration-arguments / max-length / ttl /
#      overflow-behavior 全是队列声明参数）。因此本脚本给业务队列加的 arguments
#      必须与 YAML 的 rabbit.bindings.<name>.consumer.queue-declaration-arguments 逐字一致，
#      否则应用启动报 PRECONDITION_FAILED - inequivalent arg。改一边必须改另一边。
#
# 死信设计（方案 A：broker 原生 DLX，binder 侧 auto-bind-dlq=false / republish-to-dlq=false）：
#   业务队列挂 x-dead-letter-exchange=dlx.exchange + x-dead-letter-routing-key=<队列名>.dlq，
#   本地重试耗尽后 binder 以 requeue=false 拒绝消息，由 broker 原生路由进 DLQ。
#   每个业务队列一个独立 DLQ，共用一个 direct 类型的 dlx.exchange，便于按队列定位与告警。
#   不选 binder 托管的 auto-bind-dlq：它会按 <destination>.dlq 自行命名并声明 DLX，
#   与本项目预建拓扑冲突，且会给队列追加参数导致 inequivalent arg。
#
# ⚠️ 队列 arguments 不可原地修改：
#   重跑本脚本若与线上队列参数不一致，管理 HTTP API 返回 400 bad_request（响应体含
#   "inequivalent arg"；应用走 AMQP 时则是 406 PRECONDITION_FAILED）。本脚本会先读队列积压量与消费者数再决策：
#     - 队列非空            -> 终止（删队列会丢消息），提示先排空
#     - 仍有活跃消费者    -> 终止（删队列会抽掉正在消费的队列），提示先停应用
#     - 队列为空 + 未开开关 -> 终止，并打印重建命令（默认不做任何删除）
#     - 队列为空 + 开关开启 -> 删除并重建，绑定由后续步骤重新创建
#   开关：RECREATE_EMPTY_QUEUES=1，例：
#     RECREATE_EMPTY_QUEUES=1 bash scripts/init-rabbitmq.sh
#   重建后务必同步 YAML 的 queue-declaration-arguments，否则应用侧仍报 inequivalent arg。
#
# 前置条件：
#   1. RabbitMQ 容器已启动（见 docs/infra/rabbitmq.md 第 1 步）
#   2. 延迟消息插件已启用（delayed.exchange 依赖，见文档 1.1）
#   3. test/test 账号已创建（见文档第 2 步，或用下方 RABBITMQ_USER/PASS 覆盖）
#
# 用法：
#   bash scripts/init-rabbitmq.sh
#   RABBITMQ_MGMT=http://localhost:15672 RABBITMQ_USER=admin RABBITMQ_PASS=admin bash scripts/init-rabbitmq.sh
#
# 可调环境变量（均有默认值）：
#   RABBITMQ_VHOST          vhost 路径，默认 /（脚本内自动做 URL 编码）
#   QUEUE_MAX_LENGTH        业务队列最大消息数，默认 100000
#   QUEUE_MAX_LENGTH_BYTES  业务队列最大总字节数，默认 524288000（500MB）
#   QUEUE_OVERFLOW          业务队列溢出策略，默认 reject-publish
#   DLQ_MAX_LENGTH          死信队列最大消息数，默认 50000
#   DLQ_MAX_LENGTH_BYTES    死信队列最大总字节数，默认 262144000（250MB）
#   DLQ_OVERFLOW            死信队列溢出策略，默认 reject-publish
#   DLQ_MESSAGE_TTL         死信队列消息 TTL(ms)，默认 604800000（7 天）；置 0 表示不限制
#   RECREATE_EMPTY_QUEUES   置 1 时允许删除并重建「参数不兼容且已确认为空」的队列；
#                           默认 0，即只精确报错、绝不删除
# =============================================================================
set -euo pipefail

MGMT="${RABBITMQ_MGMT:-http://localhost:15672}"
USER="${RABBITMQ_USER:-test}"
PASS="${RABBITMQ_PASS:-test}"
# vhost：默认根 vhost "/"，HTTP API 路径中需编码为 %2F
VHOST_RAW="${RABBITMQ_VHOST:-/}"
VHOST="${VHOST_RAW//\//%2F}"
API="$MGMT/api"

# 响应体落临时文件：失败时 inequivalent arg 的详细信息只在 body 里，不能丢弃
RESP_BODY="$(mktemp)"
trap 'rm -f "$RESP_BODY"' EXIT

# ---------- 队列容量与背压参数 ----------
QUEUE_MAX_LENGTH="${QUEUE_MAX_LENGTH:-100000}"
QUEUE_MAX_LENGTH_BYTES="${QUEUE_MAX_LENGTH_BYTES:-524288000}"
QUEUE_OVERFLOW="${QUEUE_OVERFLOW:-reject-publish}"
DLQ_MAX_LENGTH="${DLQ_MAX_LENGTH:-50000}"
DLQ_MAX_LENGTH_BYTES="${DLQ_MAX_LENGTH_BYTES:-262144000}"
DLQ_OVERFLOW="${DLQ_OVERFLOW:-reject-publish}"
DLQ_MESSAGE_TTL="${DLQ_MESSAGE_TTL:-604800000}"
# 删队列不可逆，默认关闭；仅显式置 1 时才允许重建「参数不兼容且为空」的队列
RECREATE_EMPTY_QUEUES="${RECREATE_EMPTY_QUEUES:-0}"

# ---------- 死信拓扑常量 ----------
DLX="dlx.exchange"   # 全局唯一死信交换机（direct 类型）
DLQ_SUFFIX=".dlq"    # 死信队列命名规则：<业务队列名>.dlq

curl_put() { # $1=资源类型 $2=名称 $3=JSON
  local rc=0 code
  code=$(curl -sS -o "$RESP_BODY" -w "%{http_code}" -u "$USER:$PASS" -X PUT \
    -H "content-type: application/json" -d "$3" "$API/$1/$VHOST/$2") || rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "[FAIL] 无法访问 $API/$1/$VHOST/$2（管理插件未启用 / 地址或账号错误 / 网络不通）"
    exit 1
  fi
  if [[ "$code" != "201" && "$code" != "204" ]]; then
    echo "[FAIL] $1/$2 -> HTTP $code"
    echo "       $(cat "$RESP_BODY")"
    echo "       提示：inequivalent arg 表示线上对象参数与本脚本不一致。"
    echo "             RabbitMQ 不允许原地修改 arguments，需排空并删除后重跑，"
    echo "             并同步更新 YAML 的 queue-declaration-arguments。"
    exit 1
  fi
  echo "[OK] $1: $2"
}

curl_bind() { # $1=交换机 $2=路由键 $3=队列
  local rc=0 code
  code=$(curl -sS -o "$RESP_BODY" -w "%{http_code}" -u "$USER:$PASS" -X POST \
    -H "content-type: application/json" -d "{\"routing_key\":\"$2\"}" \
    "$API/bindings/$VHOST/e/$1/q/$3") || rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "[FAIL] 无法访问 $API/bindings（管理插件未启用 / 地址或账号错误 / 网络不通）"
    exit 1
  fi
  if [[ "$code" != "201" && "$code" != "204" ]]; then
    echo "[FAIL] bind $1 -($2)-> $3 -> HTTP $code"
    echo "       $(cat "$RESP_BODY")"
    exit 1
  fi
  echo "[OK] bind: $1 -($2)-> $3"
}

curl_delete() { # $1=资源类型 $2=名称
  local rc=0 code
  code=$(curl -sS -o "$RESP_BODY" -w "%{http_code}" -u "$USER:$PASS" -X DELETE \
    "$API/$1/$VHOST/$2") || rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "[FAIL] 无法访问 $API/$1/$VHOST/$2（删除阶段）"
    exit 1
  fi
  # 404 视为已不存在，符合幂等语义
  if [[ "$code" != "204" && "$code" != "404" ]]; then
    echo "[FAIL] delete $1/$2 -> HTTP $code"
    echo "       $(cat "$RESP_BODY")"
    exit 1
  fi
  echo "[OK] deleted: $1/$2"
}

# 读队列积压消息数与活跃消费者数；队列不存在输出 "absent absent"，读不到输出空字符串
# 用 ?columns=name,messages,consumers 收窄响应体，避开嵌套 JSON 解析
queue_stats() { # $1=队列名
  local rc=0 code body msgs cons
  body="$(mktemp)"
  code=$(curl -sS -o "$body" -w "%{http_code}" -u "$USER:$PASS" \
    "$API/queues/$VHOST/$1?columns=name,messages,consumers") || rc=$?
  if [[ $rc -ne 0 ]]; then
    rm -f "$body"; echo ""; return 0
  fi
  if [[ "$code" == "404" ]]; then
    rm -f "$body"; echo "absent absent"; return 0
  fi
  if [[ "$code" != "200" ]]; then
    rm -f "$body"; echo ""; return 0
  fi
  # 响应形如 {"consumers":3,"messages":0,"name":"xxx"}；用 grep -o 逐个取值，
  # 避开 JSON 嵌套与花括号前缀（columns 已收窄，不会出现同名字段）
  msgs=$(grep -o '"messages":[[:space:]]*[0-9]*'  "$body" | head -1 | grep -o '[0-9]*$')
  cons=$(grep -o '"consumers":[[:space:]]*[0-9]*' "$body" | head -1 | grep -o '[0-9]*$')
  rm -f "$body"
  echo "$msgs $cons"
}

# 队列声明：把 arguments 不兼容（HTTP 400 + inequivalent arg）转成可执行的迁移决策，而不是一句看不懂的报错
put_queue() { # $1=队列名 $2=JSON
  local q="$1" payload="$2" rc=0 code msgs cons stats
  code=$(curl -sS -o "$RESP_BODY" -w "%{http_code}" -u "$USER:$PASS" -X PUT \
    -H "content-type: application/json" -d "$payload" "$API/queues/$VHOST/$q") || rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "[FAIL] 无法访问 $API/queues/$VHOST/${q}（管理插件未启用 / 地址或账号错误 / 网络不通）"
    exit 1
  fi
  if [[ "$code" == "201" || "$code" == "204" ]]; then
    echo "[OK] queues: $q"
    return 0
  fi
  # 冲突判定以响应体为准：管理 HTTP API 返回 400 bad_request（含 "inequivalent arg"），
  # 而 AMQP 协议层（应用启动时）报的是 406 PRECONDITION_FAILED。两者是同一问题的不同层面，
  # 不同 RabbitMQ 版本的状态码不一致，故不用状态码作为唯一判据。
  if ! grep -q 'inequivalent arg' "$RESP_BODY" && [[ "$code" != "406" && "$code" != "400" ]]; then
    echo "[FAIL] queues/$q -> HTTP $code"
    echo "       $(cat "$RESP_BODY")"
    echo "       提示：若为连接/权限类错误，先核对 $MGMT 与账号。"
    exit 1
  fi

  echo "[CONFLICT] queues/$q 的 arguments 与线上不一致（RabbitMQ 不允许原地修改）"
  echo "           HTTP $code: $(cat "$RESP_BODY")"
  stats="$(queue_stats "$q")"
  if [[ -z "$stats" ]]; then
    echo "[FAIL] 无法读取 ${q} 的积压量与消费者数，为避免误删已终止，请人工确认后处理。"
    exit 1
  fi
  read -r msgs cons <<< "$stats"
  if [[ "$msgs" != "0" && "$msgs" != "absent" ]]; then
    echo "[FAIL] ${q} 仍有 $msgs 条消息，为避免丢消息已终止，不会删除该队列。"
    echo "       请先排空：停掉消费者并等待消费完，或将消息迁至临时队列，再重跑本脚本。"
    exit 1
  fi
  # 活跃消费者不为 0 时绝不删：删队列会抽掉正在消费的队列，
  # 导致消费中断、channel 抖动式反复重连，且重连时序不可控
  if [[ "$cons" != "0" && "$cons" != "absent" ]]; then
    echo "[FAIL] ${q} 仍有 $cons 个活跃消费者，删除会造成消费中断与重连抖动，已终止。"
    echo "       请先停掉应用（IDEA 中停止运行/调试会话，或 kill 对应 java 进程），"
    echo "       确认 consumers=0 后重跑本脚本；迁移完成再启动应用。"
    echo "       当前可用下列命令确认："
    echo "         curl -s -u \"\$RABBITMQ_USER:\$RABBITMQ_PASS\" \\"
    echo "           \"$API/queues/$VHOST/${q}?columns=name,messages,consumers\""
    exit 1
  fi
  if [[ "$RECREATE_EMPTY_QUEUES" != "1" ]]; then
    echo "[FAIL] $q 为空（0 条消息）但参数不兼容，需删除重建。"
    echo "       本脚本默认不做删除操作。确认可以重建后执行："
    echo "         RECREATE_EMPTY_QUEUES=1 bash scripts/init-rabbitmq.sh"
    echo "       并同步 nacos/aicyi-example-rabbitmq.yml 的 queue-declaration-arguments，"
    echo "       否则应用启动仍会报 PRECONDITION_FAILED - inequivalent arg。"
    exit 1
  fi
  echo "[MIGRATE] ${q} 为空且无活跃消费者，已开启 RECREATE_EMPTY_QUEUES=1，删除并重建"
  curl_delete queues "$q"
  rc=0
  code=$(curl -sS -o "$RESP_BODY" -w "%{http_code}" -u "$USER:$PASS" -X PUT \
    -H "content-type: application/json" -d "$payload" "$API/queues/$VHOST/$q") || rc=$?
  if [[ $rc -ne 0 || ( "$code" != "201" && "$code" != "204" ) ]]; then
    echo "[FAIL] 重建 queues/$q -> HTTP $code"
    echo "       $(cat "$RESP_BODY")"
    exit 1
  fi
  echo "[OK] queues: ${q}（已重建；删队列会一并清除其绑定，绑定由后续步骤重建）"
}

# 业务队列 arguments：容量背压 + 死信路由
# $1=业务队列名（用于推导 x-dead-letter-routing-key）
queue_args() {
  printf '{"x-max-length":%s,"x-max-length-bytes":%s,"x-overflow":"%s","x-dead-letter-exchange":"%s","x-dead-letter-routing-key":"%s"}' \
    "$QUEUE_MAX_LENGTH" "$QUEUE_MAX_LENGTH_BYTES" "$QUEUE_OVERFLOW" "$DLX" "$1${DLQ_SUFFIX}"
}

# 死信队列 arguments：只限容量与 TTL，不再挂 DLX（避免死信循环投递）
dlq_args() {
  local args
  args=$(printf '{"x-max-length":%s,"x-max-length-bytes":%s,"x-overflow":"%s"' \
    "$DLQ_MAX_LENGTH" "$DLQ_MAX_LENGTH_BYTES" "$DLQ_OVERFLOW")
  if [[ "$DLQ_MESSAGE_TTL" != "0" ]]; then
    args="${args},\"x-message-ttl\":${DLQ_MESSAGE_TTL}"
  fi
  echo "${args}}"
}

# 建一组「业务队列 + 死信队列 + 死信绑定」
# $1=业务队列名
declare_queue_with_dlq() {
  local q="$1"
  local dlq="${q}${DLQ_SUFFIX}"
  put_queue "$q"   "{\"durable\":true,\"arguments\":$(queue_args "$q")}"
  put_queue "$dlq" "{\"durable\":true,\"arguments\":$(dlq_args)}"
  # direct 类型 DLX 必须精确匹配路由键，故用 DLQ 名同时作为 x-dead-letter-routing-key 与绑定键
  # 放在队列声明之后：若上方发生重建，旧绑定已被清除，此处重新建立
  curl_bind "$DLX" "$dlq" "$dlq"
}

echo "==> 创建业务交换机"
curl_put exchanges "default.exchange" '{"type":"topic","durable":true}'
curl_put exchanges "direct.exchange"  '{"type":"direct","durable":true}'
curl_put exchanges "topic.exchange"   '{"type":"topic","durable":true}'
# 延迟交换机：依赖容器内已启用的 rabbitmq_delayed_message_exchange 插件
curl_put exchanges "delayed.exchange"  '{"type":"x-delayed-message","durable":true,"arguments":{"x-delayed-type":"topic"}}'

echo "==> 创建死信交换机（全局共用一个 direct DLX，须先于业务队列创建）"
curl_put exchanges "$DLX" '{"type":"direct","durable":true}'

echo "==> 创建队列（命名与 nacos/aicyi-example-rabbitmq.yml 绑定一一对应，每个业务队列配一个 DLQ）"
declare_queue_with_dlq "default.queue"
declare_queue_with_dlq "direct.queue"
declare_queue_with_dlq "delayed.queue"
declare_queue_with_dlq "topic.exchange.order-service"
declare_queue_with_dlq "topic.exchange.log-service"

echo "==> 创建业务绑定（路由键与生产端 routing-key-expression 精确匹配）"
curl_bind "default.exchange" "#"                 "default.queue"
curl_bind "direct.exchange"  "direct.routing.key" "direct.queue"
curl_bind "delayed.exchange" "delayed.routing.key" "delayed.queue"
curl_bind "topic.exchange"   "order.#"           "topic.exchange.order-service"
curl_bind "topic.exchange"   "#"                 "topic.exchange.log-service"

echo ""
echo "==> 完成。可打开 $MGMT （$USER/****）查看拓扑"
echo "    vhost    : $VHOST_RAW"
echo "    交换机 5 个：default / direct / topic / delayed.exchange + $DLX"
echo "    队列  10 个：5 个业务队列 + 5 个 *${DLQ_SUFFIX} 死信队列"
echo "    业务队列参数：x-max-length=$QUEUE_MAX_LENGTH x-max-length-bytes=$QUEUE_MAX_LENGTH_BYTES x-overflow=$QUEUE_OVERFLOW"
echo "    死信队列参数：x-max-length=$DLQ_MAX_LENGTH x-max-length-bytes=$DLQ_MAX_LENGTH_BYTES x-overflow=$DLQ_OVERFLOW x-message-ttl=$DLQ_MESSAGE_TTL"
echo ""
echo "==> ⚠️ 生产待办"
echo "    1. x-overflow=reject-publish 在队列满时拒收新消息，必须开启 publisher-confirm"
echo "       （见 nacos/aicyi-rabbitmq.yml 生产加固建议），否则发送端无法感知被拒。"
echo "    2. 对 5 个 *${DLQ_SUFFIX} 队列深度配置监控告警：DLQ 是收容所不是兜底，进 DLQ 即代表已丢业务。"
echo "    3. delayed.queue 满时，延迟到期消息同样会被拒收丢弃，需单独关注。"
echo "    4. 本脚本的 queue_args() 与 YAML 的 queue-declaration-arguments 已同步，后续改动务必成对修改。"
