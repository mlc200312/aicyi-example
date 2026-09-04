# RabbitMQ 本地环境

在本地从零搭好 RabbitMQ（容器 + 账号 + 拓扑），供 `aicyi-example-rabbitmq` 应用与主应用的 MQ 能力使用，全程约 2 分钟。

应用连接参数在 Nacos 配置 `aicyi-rabbitmq.yml` 中：`localhost:5672`，账号 `test/test`，vhost `/`。

> **注意**：应用所有绑定均声明 `declare-exchange: false` / `bind-queue: false`，**应用不会自动创建交换机与绑定**，
> 必须先按本文初始化拓扑，否则启动报 `NOT_FOUND`。
>
> ⚠️ 但 binder **仍会幂等声明队列**（消费者属性里有 `declare-exchange` / `bind-queue`，却没有 `declare-queue` 开关，
> 而 `queue-declaration-arguments` / `max-length` / `ttl` / `overflow-behavior` 全是队列声明参数）。
> 因此脚本给业务队列设置的 `arguments` 必须与 `nacos/aicyi-example-rabbitmq.yml` 中
> `spring.cloud.stream.rabbit.bindings.<绑定名>.consumer.queue-declaration-arguments` **逐字一致**（键值与类型都要一致），
> 否则启动报 `PRECONDITION_FAILED - inequivalent arg`。**改一边必须改另一边。**

## 1. 启动 RabbitMQ 容器

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin \
  rabbitmq:3.12.14-management
```

- `5672`：AMQP 端口（应用连接）
- `15672`：管理控制台（http://localhost:15672 ，admin/admin）

### 1.1 启用延迟消息插件（延迟交换机依赖）

`delayed.exchange` 是 `x-delayed-message` 类型，需要社区插件。若容器已启用可跳过（`rabbitmq-plugins list | grep delayed` 显示 `[E*]` 即已启用）：

```bash
# 下载插件（与 RabbitMQ 版本匹配：3.12.x）到本机
curl -L -o rabbitmq_delayed_message_exchange.ez \
  https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.12.0/rabbitmq_delayed_message_exchange-3.12.0.ez

# 拷入容器并启用
docker cp rabbitmq_delayed_message_exchange.ez rabbitmq:/plugins/
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

## 2. 创建应用账号 test/test

Nacos 配置使用的应用账号不是容器默认管理员账号，需单独创建：

```bash
docker exec rabbitmq sh -c "
  rabbitmqctl add_user test test
  rabbitmqctl set_permissions -p / test '.*' '.*' '.*'
  rabbitmqctl set_user_tags test monitoring"
```

验证（返回 `{"name":"test",...}` 即成功）：

```bash
curl -s -u test:test http://localhost:15672/api/whoami
```

> 常见报错 `ACCESS_REFUSED - Login was refused using authentication mechanism PLAIN` 即为本账号缺失，按此步补齐即可。

## 3. 一键初始化拓扑

```bash
bash aicyi-example-rabbitmq/scripts/init-rabbitmq.sh
```

脚本幂等，可重复执行。若改用其他账号/地址：

```bash
RABBITMQ_MGMT=http://localhost:15672 RABBITMQ_USER=admin RABBITMQ_PASS=admin bash aicyi-example-rabbitmq/scripts/init-rabbitmq.sh
```

### 3.1 已存在队列的参数迁移

RabbitMQ **不允许原地修改已有队列的 `arguments`**。当脚本声明的参数与线上不一致时，
管理 HTTP API 返回 `400 bad_request`（响应体含 `inequivalent arg`；应用走 AMQP 时是 `406 PRECONDITION_FAILED`）。
脚本不会盲删，而是先读队列的**积压消息数**与**活跃消费者数**再分四种情况处置：

| 现场 | 脚本行为 |
| --- | --- |
| 队列非空 | 终止，提示先排空（删队列会丢消息） |
| 仍有活跃消费者 | 终止，提示先停应用（删队列会抽掉正在消费的队列） |
| 空且无消费者，但未开开关 | 终止，并打印重建命令（**默认不做任何删除**） |
| 空且无消费者，开关已开 | 删除并重建，绑定由后续步骤重新建立 |

开关为 `RECREATE_EMPTY_QUEUES=1`。**标准迁移流程**（四步顺序不可颠倒）：

```bash
# 1. 停应用：IDEA 中停止运行/调试会话，或 kill 对应 java 进程
# 2. 确认已无消费者（期望 5 个业务队列 consumers 均为 0）
curl -s -u test:test 'http://localhost:15672/api/queues/%2F' \
  | grep -o '"name":"[^"]*"\|"consumers":[0-9]*\|"messages":[0-9]*'

# 3. 带开关重跑，完成删除重建
RECREATE_EMPTY_QUEUES=1 bash aicyi-example-rabbitmq/scripts/init-rabbitmq.sh

# 4. 确认 5 个业务队列均已挂上 DLX 参数、且 5 个 *.dlq 已建立后，再启动应用
```

> ⚠️ 切勿在应用运行时带开关执行：虽然 Spring AMQP 会自动重连，但删队列会抽掉正在消费的队列，
> 造成秒级消费中断且重连时序不可控。新版脚本已内置消费者守卫会拦下这种操作，但不要依赖它。

## 4. 拓扑说明（与配置对应关系）

拓扑依据 `nacos/aicyi-example-rabbitmq.yml`（绑定与生产端 `routing-key-expression`）推导。

### 交换机

| 交换机 | 类型 | 说明 |
| --- | --- | --- |
| `default.exchange` | topic | 默认消息 |
| `direct.exchange` | direct | 点对点，路由键精确匹配 |
| `topic.exchange` | topic | 按路由键模式分发到多个消费组 |
| `delayed.exchange` | x-delayed-message（x-delayed-type=topic） | 延迟消息，需 1.1 的插件 |
| `dlx.exchange` | direct | 全局共用死信交换机，路由键=DLQ 名；须先于业务队列创建 |

### 队列与绑定

| 队列 | 绑定到 | 路由键 | 死信队列 | 对应绑定 / 说明 |
| --- | --- | --- | --- | --- |
| `default.queue` | default.exchange | `#` | `default.queue.dlq` | `messageInput-in-0`（`queue-name-group-only: true` → 队列名=group） |
| `direct.queue` | direct.exchange | `direct.routing.key` | `direct.queue.dlq` | `directInput-in-0`；生产端静态路由键 `'direct.routing.key'`，direct 交换机必须精确匹配 |
| `delayed.queue` | delayed.exchange | `delayed.routing.key` | `delayed.queue.dlq` | `delayedInput-in-0`；生产端静态路由键 `'delayed.routing.key'`，延迟时长由消息头 `x-delay`（毫秒）决定 |
| `topic.exchange.order-service` | topic.exchange | `order.#` | `topic.exchange.order-service.dlq` | `orderEvents-in-0`（group=order-service，默认队列名=destination.group），Handler 再按 `routingKey` 头分发 `order.created`/`order.paid` |
| `topic.exchange.log-service` | topic.exchange | `#` | `topic.exchange.log-service.dlq` | `systemLogs-in-0`（group=log-service），接收全部系统日志 |

每个 DLQ 都以自身队列名作为路由键绑定到 `dlx.exchange`（direct 类型需精确匹配）。

### 队列 arguments（脚本与 YAML 双向声明，必须一致）

| 队列类型 | x-max-length | x-max-length-bytes | x-overflow | x-message-ttl | 死信路由 |
| --- | --- | --- | --- | --- | --- |
| 业务队列 ×5 | 100000 | 524288000（500MB） | reject-publish | — | `dlx.exchange` + `<队列名>.dlq` |
| 死信队列 ×5 | 50000 | 262144000（250MB） | reject-publish | 604800000（7 天） | 不挂（避免死信循环投递） |

均可用环境变量覆盖：`QUEUE_MAX_LENGTH` / `QUEUE_MAX_LENGTH_BYTES` / `QUEUE_OVERFLOW` /
`DLQ_MAX_LENGTH` / `DLQ_MAX_LENGTH_BYTES` / `DLQ_OVERFLOW` / `DLQ_MESSAGE_TTL`（置 0 表示不限 TTL），详见脚本头部。

> ⚠️ 覆盖后必须同步修改 YAML 的 `queue-declaration-arguments`，否则触发 `inequivalent arg`。
> 且 RabbitMQ **不允许原地修改已有队列的 arguments**，需删除重建，流程见 3.1。

> 绑定名按 Spring Cloud Stream 4.x 函数式模型派生为 `<函数名>-in-0`（函数名见
> `spring.cloud.function.definition`，5 个 Consumer Bean 定义在 `MessageConfiguration`），
> 不再是旧版的 `message-input` / `direct-input` / `delayed-input`。
>
> ✅ 历史上曾有两处配置**静默失效**，现已修正，请勿回退：
>
> 1. 函数定义曾写在 `spring.cloud.stream.function.definition`。SCS 4.1.3 中该前缀只有 `bindings`
>    一个字段（`StreamFunctionConfigurationProperties`），`definition` 会被静默忽略 →
>    5 个消费者全部不绑定。正确位置是 `spring.cloud.function.definition`
>    （由 spring-cloud-function-context 提供，已从 jar 配置元数据核实）。
> 2. `spring.cloud.stream.bindings` 下曾用旧名 `message-input` / `delayed-input` / `direct-input`，
>    与 `spring.cloud.stream.rabbit.bindings` 的新名不一致，其 `destination` / `group` / `concurrency`
>    均不生效。现两处已统一为 `<函数名>-in-0`。

### 队列命名规则速记

- 配置了 `queue-name-group-only: true`：队列名 = group（如 `default.queue`）
- 未配置：队列名 = `destination.group`（如 `topic.exchange.order-service`）

### 死信链路（broker 原生 DLX）

采用**方案 A：broker 原生 DLX**，而非 binder 托管的 `auto-bind-dlq` —— 后者会按 `<destination>.dlq`
自行命名并声明 DLX，与本项目预建拓扑冲突，还会给队列追加参数导致 `inequivalent arg`。

```
消息 → 业务队列 → 消费抛异常 → 本地重试（默认 3 次）耗尽
     → binder 以 requeue=false 拒绝 → broker 按 x-dead-letter-* 路由
     → dlx.exchange（direct，路由键 = DLQ 名）→ <业务队列名>.dlq
```

配套的 YAML 关键项（`rabbit.bindings.<绑定名>.consumer`）：

| 配置 | 取值 | 作用 |
| --- | --- | --- |
| `auto-bind-dlq` | `false`（默认，已注释） | DLX/DLQ 由脚本预建，binder 不托管 |
| `republish-to-dlq` | `false`（**显式声明**） | 默认 `true` 时 binder 会自行重投到本拓扑中不存在的 `<destination>.dlq`，导致消息彻底丢失 |
| `requeue-rejected` | `false`（默认，已注释） | 重试耗尽后 reject 且不重回队列，才会触发 DLX |

> - `republish-to-dlq: false` 的代价：DLQ 中的消息不带 `x-exception-stacktrace` 头，异常堆栈需业务侧自行落日志。
> - 消费链路的异常不会被吞：`MessagingExceptionHandler` 是 `@ControllerAdvice`，只作用于 MVC 请求链，
>   不拦截 Stream 消费者调用，因此异常能正常传播并触发重试与死信。
> - ⚠️ **DLQ 是收容所不是兜底**：消息进 DLQ 即代表业务已失败，须对 5 个 `*.dlq` 队列深度配置监控告警。
> - `x-overflow=reject-publish` 在队列满时拒收新消息，必须开启 publisher-confirm（见 `nacos/aicyi-rabbitmq.yml`），
>   否则发送端无法感知被拒；`delayed.queue` 满时延迟到期消息同样会被丢弃，需单独关注。

## 5. 验证

1. 管理控制台查看：http://localhost:15672 → Exchanges/Queues，确认 **5 个交换机**（4 业务 + `dlx.exchange`）、
   **10 个队列**（5 业务 + 5 `*.dlq`）、绑定齐全（5 业务绑定 + 5 死信绑定）。
   业务队列详情页应能看到 `x-dead-letter-exchange` / `x-max-length` 等 Features 标记。
2. 启动 `AicyiExampleRabbitmqApplication`，日志无 `NOT_FOUND`/`ACCESS_REFUSED`，各绑定出现消费者即成功。
3. 发送测试消息可运行 `aicyi-example-rabbitmq` 的 `MqSenderTest`（路由键 `order.created`、`order.paid`、`system.test` 分别验证 topic 分发）。

## 常见问题

| 报错 | 原因 | 处理 |
| --- | --- | --- |
| `ACCESS_REFUSED ... PLAIN` | 应用账号不存在 | 执行第 2 步 |
| `NOT_FOUND - no exchange/queue ...` | 拓扑未初始化 | 执行第 3 步 |
| `PRECONDITION_FAILED - inequivalent arg ...`（应用侧）<br>`[CONFLICT] ... HTTP 400`（脚本侧） | 队列已存在，但 `arguments` 与本次声明不一致（RabbitMQ 不允许原地改 arguments） | 按 3.1 迁移流程：停应用 → `RECREATE_EMPTY_QUEUES=1` 重跑 → 启动应用；并确认 YAML 的 `queue-declaration-arguments` 已同步 |
| `[FAIL] ... 仍有 N 个活跃消费者` | 应用未停，队列上还有消费者 | 先停应用，确认 `consumers=0` 后重跑 |
| `[FAIL] ... 仍有 N 条消息` | 队列非空，脚本拒绝删除以免丢消息 | 先排空（停消费者等其消费完，或迁至临时队列）后重跑 |
| `operation ... involving 'x-delayed-message' ... plugin not enabled` | 延迟插件未启用 | 执行 1.1 |
| `Connection refused` | 容器未启动/端口未映射 | `docker ps` 检查 5672 映射 |

## 相关文档

- [aicyi-example-rabbitmq 应用说明](../apps/aicyi-example-rabbitmq.md)
- [Nacos 配置中心](./nacos.md)
