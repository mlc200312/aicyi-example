# RabbitMQ 本地环境快速启动

本文档帮助新人在本地从零搭好 `aicyi-example-rabbitmq` 所需的 RabbitMQ 环境（容器 + 账号 + 拓扑），全程约 2 分钟。

应用连接参数在 Nacos 配置 `aicyi-rabbitmq.yml` 中：`localhost:5672`，账号 `test/test`，vhost `/`。

> 注意：应用所有绑定均声明 `declare-exchange: false` / `bind-queue: false`，**应用不会自动创建交换机、队列与绑定**，必须先按本文初始化拓扑，否则启动报 `NOT_FOUND`。

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
bash scripts/init-rabbitmq.sh
```

脚本幂等，可重复执行。若改用其他账号/地址：

```bash
RABBITMQ_MGMT=http://localhost:15672 RABBITMQ_USER=admin RABBITMQ_PASS=admin bash scripts/init-rabbitmq.sh
```

## 4. 拓扑说明（与配置对应关系）

拓扑依据 `aicyi-example-rabbitmq/src/main/resources/application.yml`（消费端绑定）与
`aicyi-example-rabbitmq/nacos/aicyi-example-rabbitmq.yml`（生产端 `routing-key-expression`）推导：

### 交换机

| 交换机 | 类型 | 说明 |
| --- | --- | --- |
| `default.exchange` | topic | 默认消息 |
| `direct.exchange` | direct | 点对点，路由键精确匹配 |
| `topic.exchange` | topic | 按路由键模式分发到多个消费组 |
| `delayed.exchange` | x-delayed-message（x-delayed-type=topic） | 延迟消息，需 1.1 的插件 |

### 队列与绑定

| 队列 | 绑定到 | 路由键 | 对应绑定 / 说明 |
| --- | --- | --- | --- |
| `default.queue` | default.exchange | `#` | `message-input`（`queue-name-group-only: true` → 队列名=group） |
| `direct.queue` | direct.exchange | `direct.routing.key` | `direct-input`；生产端静态路由键 `'direct.routing.key'`，direct 交换机必须精确匹配 |
| `delayed.queue` | delayed.exchange | `delayed.routing.key` | `delayed-input`；生产端静态路由键 `'delayed.routing.key'`，延迟时长由消息头 `x-delay`（毫秒）决定 |
| `topic.exchange.order-service` | topic.exchange | `order.#` | `orderEvents-in-0`（group=order-service，默认队列名=destination.group），Handler 再按 `routingKey` 头过滤 `order.created`/`order.paid` |
| `topic.exchange.log-service` | topic.exchange | `#` | `systemLogs-in-0`（group=log-service），接收全部系统日志 |

### 队列命名规则速记

- 配置了 `queue-name-group-only: true`：队列名 = group（如 `default.queue`）
- 未配置：队列名 = `destination.group`（如 `topic.exchange.order-service`）

## 5. 验证

1. 管理控制台查看：http://localhost:15672 → Exchanges/Queues，确认 4 个交换机、5 个队列、绑定齐全。
2. 启动 `AicyiExampleRabbitmqApplication`，日志无 `NOT_FOUND`/`ACCESS_REFUSED`，各绑定出现消费者即成功。
3. 发送测试消息可运行 `aicyi-example-rabbitmq` 的 `MqSenderTest`（路由键 `order.created`、`order.paid`、`system.test` 分别验证 topic 分发）。

## 常见问题

| 报错 | 原因 | 处理 |
| --- | --- | --- |
| `ACCESS_REFUSED ... PLAIN` | 应用账号不存在 | 执行第 2 步 |
| `NOT_FOUND - no exchange/queue ...` | 拓扑未初始化 | 执行第 3 步 |
| `operation ... involving 'x-delayed-message' ... plugin not enabled` | 延迟插件未启用 | 执行 1.1 |
| `Connection refused` | 容器未启动/端口未映射 | `docker ps` 检查 5672 映射 |
