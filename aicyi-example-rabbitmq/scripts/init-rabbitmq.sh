#!/usr/bin/env bash
# =============================================================================
# RabbitMQ 本地开发环境初始化脚本（幂等，可重复执行）
#
# 前置条件：
#   1. RabbitMQ 容器已启动（见 docs/infra/rabbitmq.md 第 1 步）
#   2. test/test 账号已创建（见文档第 2 步，或直接使用下方 RABBITMQ_USER 覆盖）
#
# 用法：
#   bash scripts/init-rabbitmq.sh
#   RABBITMQ_MGMT=http://localhost:15672 RABBITMQ_USER=admin RABBITMQ_PASS=admin bash scripts/init-rabbitmq.sh
# =============================================================================
set -euo pipefail

MGMT="${RABBITMQ_MGMT:-http://localhost:15672}"
USER="${RABBITMQ_USER:-test}"
PASS="${RABBITMQ_PASS:-test}"
VHOST="%2F"
API="$MGMT/api"

curl_put() { # $1=路径 $2=名称 $3=JSON
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" -u "$USER:$PASS" -X PUT \
    -H "content-type: application/json" -d "$3" "$API/$1/$VHOST/$2")
  [[ "$code" == "201" || "$code" == "204" ]] || { echo "[FAIL] $1/$2 -> HTTP $code"; exit 1; }
  echo "[OK] $1: $2"
}

curl_bind() { # $1=交换机 $2=路由键 $3=队列
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" -u "$USER:$PASS" -X POST \
    -H "content-type: application/json" -d "{\"routing_key\":\"$2\"}" \
    "$API/bindings/$VHOST/e/$1/q/$3")
  [[ "$code" == "201" || "$code" == "204" ]] || { echo "[FAIL] bind $1 -($2)-> $3 -> HTTP $code"; exit 1; }
  echo "[OK] bind: $1 -($2)-> $3"
}

echo "==> 创建交换机"
curl_put exchanges "default.exchange" '{"type":"topic","durable":true}'
curl_put exchanges "direct.exchange"  '{"type":"direct","durable":true}'
curl_put exchanges "topic.exchange"   '{"type":"topic","durable":true}'
# 延迟交换机：依赖容器内已启用的 rabbitmq_delayed_message_exchange 插件
curl_put exchanges "delayed.exchange"  '{"type":"x-delayed-message","durable":true,"arguments":{"x-delayed-type":"topic"}}'

echo "==> 创建队列（命名与 application.yml 绑定一一对应）"
curl_put queues "default.queue"                  '{"durable":true}'
curl_put queues "direct.queue"                   '{"durable":true}'
curl_put queues "delayed.queue"                  '{"durable":true}'
curl_put queues "topic.exchange.order-service"   '{"durable":true}'
curl_put queues "topic.exchange.log-service"     '{"durable":true}'

echo "==> 创建绑定（路由键与生产端 routing-key-expression 精确匹配）"
curl_bind "default.exchange" "#"                 "default.queue"
curl_bind "direct.exchange"  "direct.routing.key" "direct.queue"
curl_bind "delayed.exchange" "delayed.routing.key" "delayed.queue"
curl_bind "topic.exchange"   "order.#"           "topic.exchange.order-service"
curl_bind "topic.exchange"   "#"                 "topic.exchange.log-service"

echo ""
echo "==> 完成。可打开 $MGMT （$USER/****）查看拓扑"
