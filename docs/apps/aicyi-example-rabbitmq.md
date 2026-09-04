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

默认以 `test` profile 启动（`application.yml` 中写死）。以生产 profile 启动需覆盖：

```bash
SPRING_PROFILES_ACTIVE=prod NACOS_NAMESPACE=<prod-namespace-id> mvn spring-boot:run
# 或 java -jar target/aicyi-example-rabbitmq-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 配置

### 本地 `application.yml`

- `server.port: 8082`，`spring.profiles.active: test`
- Nacos 地址：`${NACOS_SERVER_ADDR:127.0.0.1:8848}`

### profile 与 namespace

| profile | 配置文件 | namespace | 说明 |
| --- | --- | --- | --- |
| test | `application-test.yml` | `${NACOS_NAMESPACE:29b4684f-0751-4290-a0a4-f65a51893ef6}` | 内置默认值，本地开箱可用 |
| prod | `application-prod.yml` | `${NACOS_NAMESPACE}` | 必须由环境变量注入，未设置则启动失败，防止误连其他环境 |

### Nacos 导入清单（test / prod 相同）

| Data ID | 作用 |
| --- | --- |
| `aicyi-example-rabbitmq.yml` | 共享：消费函数声明（`function.definition`）、Stream 绑定（bindings）、交换机类型与路由键表达式 |
| `aicyi-rabbitmq.yml` | RabbitMQ 连接与 binder。仓库内为本地基线（`localhost:5672`，账号 `test/test`）；prod namespace 的副本须改为生产集群参数与专用账号 |

> 两个 profile 导入的 Data ID 完全一致，差异只在 namespace 来源。
> 生产上线前置检查清单（插件、拓扑预建、凭证注入、集群高可用策略）见 `application-prod.yml` 文件头注释。

### 通道常量（`channel` 包）

两个都是**纯常量接口**：Spring Cloud Stream 4.x 已移除 `@Input` / `@Output` / `@EnableBinding` / `@StreamListener`
注解式编程模型（4.1.3 的 jar 中已无这些类型），通道名改为普通字符串常量集中维护。

| 接口 | 常量值 | 说明 |
| --- | --- | --- |
| `OutputMessageChannels` | `message-output` / `delayed-output` / `direct-output` / `topic-output` | 生产端绑定名，`MqSender` 按此名发送 |
| `InputMessageChannels` | `messageInput` / `delayedInput` / `directInput` / `orderEvents-in-0` / `systemLogs-in-0` | 消费端**函数名**，绑定名自动派生为 `<函数名>-in-0` |

## 生产者

`MessageConfiguration` 提供 JSON `MessageConverter`，并以 `Consumer<T>` 函数 Bean 声明全部消费者（见下节）。
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

消费者以 `Consumer<T>` 函数 Bean 声明在 `MessageConfiguration` 中，方法引用指向 `handler` 包的实际处理逻辑：

| 函数 Bean（绑定名） | Handler 方法 | 入参类型 | 说明 |
| --- | --- | --- | --- |
| `messageInput`（`messageInput-in-0`） | `MessageHandlers#handleMessage` | `UserBean` | 默认消息 |
| `directInput`（`directInput-in-0`） | `DirectMessageHandlers#handleMessage` | `Message<UserBean>` | 直连消息 |
| `delayedInput`（`delayedInput-in-0`） | `DelayedMessageHandlers#handleMessage` | `Message<UserBean>` | 延迟消息 |
| `orderEvents`（`orderEvents-in-0`） | `TopicMessageHandlers#handleOrderEvent` | `Message<UserBean>` | 订单事件，方法内按 `routingKey` 头分发到 `orderEventsCreated` / `orderEventsPaid` |
| `systemLogs`（`systemLogs-in-0`） | `TopicMessageHandlers#systemLogs` | `Message<UserBean>` | 系统日志 |

> 旧版 `@StreamListener(condition = "...")` 的 SpEL 条件路由在函数式模型中不再支持，
> 故 `order.created` / `order.paid` 的过滤下沉到 `handleOrderEvent` 方法内按消息头自行分发。
> 需要读取消息头时用 `Message<T>` 包装入参，只关心消息体时可直接用 `T`（如 `messageInput`）。

`aop/MessagingExceptionHandler` 统一处理消息发送/转换/处理异常。

## 测试

`MqSenderTest` 覆盖四种通道的消息发送与路由键验证：

```bash
mvn test -pl aicyi-example-rabbitmq
```

## 消息拓扑（交换机/队列/绑定）

拓扑由 `nacos/aicyi-example-rabbitmq.yml` 推导，初始化脚本见 `aicyi-example-rabbitmq/scripts/init-rabbitmq.sh`，
明细表见 [RabbitMQ 本地环境 - 拓扑说明](../infra/rabbitmq.md#4-拓扑说明与配置对应关系)。
