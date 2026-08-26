# aicyi-example-rabbitmq（RabbitMQ 消息应用）

演示基于 Spring Cloud Stream 的 RabbitMQ 生产/消费：直连、主题（动态路由键）、延迟消息。

- 端口：**8082**
- 启动类：`io.github.aicyi.example.rabbitmq.AicyiExampleRabbitmqApplication`

> 该模块由原 `aicyi-example-consumer` 重构而来，生产通道与消费 Handler 都位于此模块内。

## 前置：RabbitMQ 环境

应用所有绑定均声明 `declare-exchange: false` / `bind-queue: false`，**不会自动创建交换机/队列/绑定**，
必须先初始化拓扑，否则启动报 `NOT_FOUND`。完整搭建见 [RabbitMQ 本地环境](../infra/rabbitmq.md)
（容器 + 账号 + 延迟插件 + `scripts/init-rabbitmq.sh` 一键初始化）。

## 启动

```bash
cd aicyi-example-rabbitmq
mvn spring-boot:run
```

## 配置

### 本地 `application.yml`

- `server.port: 8082`，`spring.profiles.active: test`

### Nacos 导入清单（`application-test.yml`）

| Data ID | 作用 |
| --- | --- |
| `aicyi-example-rabbitmq.yml` | 共享：Stream 绑定（bindings）、交换机类型与路由键表达式 |
| `aicyi-rabbitmq.yml` | RabbitMQ 连接与 binder（`localhost:5672`，账号 `test/test`） |

### 通道定义（`channel` 包）

| 接口 | 通道名 | 说明 |
| --- | --- | --- |
| `OutputMessageChannels` | `message-output` / `delayed-output` / `direct-output` / `topic-output` | 生产端（`@Output`） |
| `InputMessageChannels` | `message-input` / `delayed-input` / `direct-input` / `orderEvents-in-0` / `systemLogs-in-0` | 消费端（`@Input`） |

## 生产者

`MessageConfiguration` 通过 `@EnableBinding` 绑定全部通道，并提供 JSON `MessageConverter`。
生产可直接注入 `MqSender`（aicyi-midware-rabbitmq 的 `StreamMqSender` 自动装配）：

```java
@Autowired
private MqSender mqSender;

// 默认消息
mqSender.send(OutputMessageChannels.OUTPUT, userBean);

// 主题消息：路由键由 headers['routingKey'] 决定
mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean,
        Maps.of("routingKey", "order.created").build());

// 延迟消息（x-delay 头，毫秒）
mqSender.sendDelayed(OutputMessageChannels.DELAYED_OUTPUT, userBean, 5000L);
```

> 也可通过统一消息入口 `UnifiedMessageManager` 发送 `MqMessage`（destination 填通道名），
> 示例见主应用 `aicyi-example-boot` 的验证码发送逻辑。

## 消费者（`handler` 包）

| Handler | 通道 | 说明 |
| --- | --- | --- |
| `MessageHandlers` | `message-input` | 默认消息，接收 `UserBean` |
| `DirectMessageHandlers` | `direct-input` | 直连消息 |
| `DelayedMessageHandlers` | `delayed-input` | 延迟消息 |
| `TopicMessageHandlers` | `orderEvents-in-0` / `systemLogs-in-0` | 主题消息，按 `headers['routingKey']` 条件过滤 `order.created` / `order.paid` |

`aop/MessagingExceptionHandler` 统一处理消息发送/转换/处理异常。

## 测试

`MqSenderTest` 覆盖四种通道的消息发送与路由键验证：

```bash
mvn test -pl aicyi-example-rabbitmq
```

## 消息拓扑（交换机/队列/绑定）

拓扑由 `nacos/aicyi-example-rabbitmq.yml` 推导，初始化脚本见 `aicyi-example-rabbitmq/scripts/init-rabbitmq.sh`，
明细表见 [RabbitMQ 本地环境 - 拓扑说明](../infra/rabbitmq.md#4-拓扑说明与配置对应关系)。
