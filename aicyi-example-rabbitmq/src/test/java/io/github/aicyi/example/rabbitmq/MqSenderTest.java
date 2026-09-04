package io.github.aicyi.example.rabbitmq;

import io.github.aicyi.commons.util.map.Maps;
import io.github.aicyi.example.domain.bo.UserBean;
import io.github.aicyi.example.domain.util.BaseLoggerTest;
import io.github.aicyi.example.domain.util.DataSource;
import io.github.aicyi.example.rabbitmq.channel.OutputMessageChannels;
import io.github.aicyi.midware.message.core.exception.MessageResultCode;
import io.github.aicyi.midware.message.core.exception.MessageSendException;
import io.github.aicyi.midware.message.mq.model.MqMessage;
import io.github.aicyi.midware.message.mq.sender.MqSender;
import io.github.aicyi.commons.core.message.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MQ 消息发送测试（生产者侧）。
 * <p>
 * <b>两种发送方式</b>：{@link StreamBridge} 是 Spring Cloud Stream 4.x 的原生入口；
 * {@link MqSender} 是 aicyi 的封装（{@code StreamMqSender} 内部同样委托 StreamBridge），
 * 额外提供统一的异常语义、延迟发送与批量发送。
 * <p>
 * <b>{@code send} 的返回值语义</b>：{@code StreamMqSender#send} 成功恒返回 {@code true}，
 * 失败<b>不会</b>返回 {@code false}，而是抛 {@link MessageSendException}（code=40013）。
 * 所以不要写 {@code if (!mqSender.send(...))} 这种分支，失败路径只能靠捕获异常。
 * 而 {@code streamBridge.send} 返回 {@code true} 仅代表消息已交给 binder 的 MessageChannel，
 * <b>不代表 broker 已接受</b>（AMQP 异步，交换机不存在之类的 channel 级错误是稍后回报的）；
 * 要确认消息真的落入队列，需查管理台 {@code http://localhost:15672} 对应队列的 ready 数。
 * <p>
 * <b>生产端拓扑</b>（{@code nacos/aicyi-example-rabbitmq.yml} + {@code scripts/init-rabbitmq.sh}）：
 * <ul>
 *     <li>{@code message-output} → {@code default.exchange}(topic)，无 routing-key-expression → <b>空路由键</b>，
 *         经 {@code #} 绑定进 {@code default.queue}</li>
 *     <li>{@code direct-output} → {@code direct.exchange}(direct)，静态路由键 {@code direct.routing.key} → {@code direct.queue}</li>
 *     <li>{@code delayed-output} → {@code delayed.exchange}(x-delayed-message)，静态路由键 {@code delayed.routing.key} → {@code delayed.queue}</li>
 *     <li>{@code topic-output} → {@code topic.exchange}(topic)，<b>动态路由键 {@code headers['routingKey']}</b>；
 *         该 exchange 有两条绑定：{@code order.#} → {@code topic.exchange.order-service}，{@code #} → {@code topic.exchange.log-service}</li>
 * </ul>
 * <p>
 * <b>已知问题（影响消费端，不影响本类的发送用例）</b>：{@code spring.cloud.stream.bindings} 下
 * {@code message-input}/{@code delayed-input}/{@code direct-input} 仍是注解式模型的旧绑定名，
 * 而函数式模型生效的绑定名是 {@code messageInput-in-0} 等（只出现在 {@code stream.rabbit.bindings} 下），
 * 因此这三个消费者的 {@code destination}/{@code group} 未生效 → 消费者监听的不是 {@code default.exchange} 等目标交换机，
 * 表现为 {@code default.queue}/{@code direct.queue}/{@code delayed.queue} 只堆积不消费。
 * {@code orderEvents-in-0}/{@code systemLogs-in-0} 两处命名一致，配置正常。
 *
 * @author Mr.Min
 * @description MQ 消息发送测试
 * @date 20:30
 **/
@SpringBootTest(classes = AicyiExampleRabbitmqApplication.class)
public class MqSenderTest extends BaseLoggerTest {

    /**
     * Spring Cloud Stream 4.x 函数式模型：@EnableBinding 注入的 MessageChannel Bean 已不存在，
     * 生产者统一通过 StreamBridge 按绑定名发送
     */
    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private MqSender mqSender;

    @Override
    public void beforeTest() {
    }

    @Test
    @Override
    public void test() {

        UserBean userBean = DataSource.getUser();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).build();

        // 方式一：StreamBridge 原生入口，按绑定名发送
        assertTrue(streamBridge.send(OutputMessageChannels.OUTPUT, message));
        // 注意：这条消息未带 routingKey 头，而 topic-output 的 routing-key-expression 是 headers['routingKey']，
        // 求值为 null → 以空路由键投递到 topic.exchange，只被 # 绑定（log-service）接收，order.# 不匹配
        assertTrue(streamBridge.send(OutputMessageChannels.TOPIC_OUTPUT, message));
        assertTrue(streamBridge.send(OutputMessageChannels.DIRECT_OUTPUT, message));

        // 方式二：MqSender 封装，成功恒返回 true（失败抛 MessageSendException，不会返回 false）
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, userBean));
        assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean));
        assertTrue(mqSender.send(OutputMessageChannels.DIRECT_OUTPUT, userBean));
    }

    /**
     * 带业务头的发送：{@code routingKey} 头被 {@code topic-output} 的
     * {@code routing-key-expression: headers['routingKey']} 取作 AMQP 路由键；
     * 对 {@code message-output}/{@code direct-output} 而言该头只是普通消息头（路由键由配置静态决定）。
     */
    @Test
    public void test2() {
        UserBean userBean = DataSource.getUser();

        Map<String, Object> headers = Maps.of("routingKey", (Object) "order.created").build();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).copyHeaders(headers).build();

        // 方式一
        assertTrue(streamBridge.send(OutputMessageChannels.OUTPUT, message));
        assertTrue(streamBridge.send(OutputMessageChannels.TOPIC_OUTPUT, message));
        assertTrue(streamBridge.send(OutputMessageChannels.DIRECT_OUTPUT, message));

        //方式二
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, userBean, headers));
        assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers));
        assertTrue(mqSender.send(OutputMessageChannels.DIRECT_OUTPUT, userBean, headers));
    }

    /**
     * 延迟消息：{@code x-delay} 头由 {@code delayed.exchange}（{@code x-delayed-message} 类型，
     * 依赖容器内已启用的 {@code rabbitmq_delayed_message_exchange} 插件）识别，
     * 消息先在交换机内滞留指定毫秒数，再按 {@code delayed.routing.key} 投递到 {@code delayed.queue}。
     * <p>
     * {@code sendDelayed} 本质就是 {@code send} 加上 {@code x-delay} 头，因此下面三种写法等价。
     */
    @Test
    public void test3() {
        UserBean userBean = DataSource.getUser();

        long delayMillis = 10 * 1000;

        Map<String, Object> headers = Maps.of("x-delay", (Object) delayMillis).build();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).copyHeaders(headers).build();
        // 方式一：手工设置 x-delay 头
        assertTrue(streamBridge.send(OutputMessageChannels.DELAYED_OUTPUT, message));

        // 方式二
        // 立即发送（不带 x-delay → 不延迟）
        assertTrue(mqSender.send(OutputMessageChannels.DELAYED_OUTPUT, userBean));
        // 延迟发送（内部即 send(channel, payload, {"x-delay": delayMillis})）
        assertTrue(mqSender.sendDelayed(OutputMessageChannels.DELAYED_OUTPUT, userBean, delayMillis));
    }

    /**
     * topic 多路由键分发：三个 routingKey 都发往 {@code topic.exchange}，按绑定规则分流。
     * <p>
     * 消费端 {@code TopicMessageHandlers#handleOrderEvent} 再按 {@code routingKey} 头在方法内二次分发
     * （取代旧版 {@code @StreamListener(condition = "...")} 的 SpEL 路由）：
     * {@code order.created} → {@code orderEventsCreated}，{@code order.paid} → {@code orderEventsPaid}，
     * 其余（如 {@code system.test}）走 else 分支打 Ignored 日志。
     */
    @Test
    public void test4() {
        UserBean userBean = DataSource.getUser();

        Map<String, Object> headers = Maps.of("routingKey", (Object) "order.created").build();
        Map<String, Object> headers2 = Maps.of("routingKey", (Object) "order.paid").build();
        Map<String, Object> headers3 = Maps.of("routingKey", (Object) "system.test").build();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).copyHeaders(headers).build();
        Message<UserBean> message2 = MessageBuilder.withPayload(userBean).copyHeaders(headers2).build();
        Message<UserBean> message3 = MessageBuilder.withPayload(userBean).copyHeaders(headers3).build();

        // 方式一
        assertTrue(streamBridge.send(OutputMessageChannels.TOPIC_OUTPUT, message));
        assertTrue(streamBridge.send(OutputMessageChannels.TOPIC_OUTPUT, message2));
        assertTrue(streamBridge.send(OutputMessageChannels.TOPIC_OUTPUT, message3));

        // 方式二
        assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers));
        assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers2));
        assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers3));
    }

    // ============================================================================
    // 以下为补充用例：覆盖 MqSender 完整接口契约、MqMessage 模型桥接与路由边界
    // ============================================================================

    /**
     * 批量发送：{@link MqSender} 的第 4 个接口方法，原 {@code test}~{@code test4} 均未覆盖。
     * <p>
     * {@code sendBatch} 的实现就是对列表逐条 {@code send}，因此返回列表长度必然等于入参长度、
     * 元素恒为 {@code true}。
     * <p>
     * <b>它不是原子操作</b>：中途某条抛 {@link MessageSendException} 会直接向上中断，
     * 已发出的消息不会回滚，调用方也拿不到返回列表（想逐条容错得自己循环 + try/catch）。
     */
    @Test
    public void testSendBatch() {
        List<UserBean> payloads = Arrays.asList(DataSource.getUser(), DataSource.getUser(), DataSource.getUser());

        List<Boolean> results = mqSender.sendBatch(OutputMessageChannels.OUTPUT, payloads);

        assertNotNull(results);
        assertEquals(payloads.size(), results.size(), "返回列表长度应等于入参长度");
        assertTrue(results.stream().allMatch(Boolean::booleanValue), "逐条发送应全部成功");
        log(results);

        // 边界：空列表 → 返回空 List，不抛异常也不发消息
        List<Boolean> emptyResults = mqSender.sendBatch(OutputMessageChannels.OUTPUT, Collections.emptyList());
        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());
    }

    /**
     * 异常契约：payload 为 null 时的行为，同时印证“失败走异常、不返回 false”。
     * <p>
     * {@code MessageBuilder.withPayload(null)} 会抛 {@link IllegalArgumentException}（Spring 的 Assert.notNull），
     * 被 {@code StreamMqSender} 的 {@code catch (Exception)} 捕获后包装成 {@link MessageSendException}，
     * code 固定为 {@link MessageResultCode#MESSAGE_SEND_ERROR}（40013），原始异常保留在 cause 里。
     */
    @Test
    public void testSendNullPayloadThrows() {
        MessageSendException e = assertThrows(MessageSendException.class,
                () -> mqSender.send(OutputMessageChannels.OUTPUT, null));

        assertEquals(MessageResultCode.MESSAGE_SEND_ERROR.getCode(), e.getCode());
        assertNotNull(e.getCause(), "原始异常应保留在 cause 中");
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
        log(e.getCode(), e.getMessage());
    }

    /**
     * {@link MqMessage} 模型桥接。
     * <p>
     * <b>{@link MqSender} 并没有 {@code send(MqMessage)} 重载</b> —— 接口只认
     * {@code (channel, payload, headers)}，而 {@link MqMessage} 自带 destination/group/routingKey/delay
     * 四个字段，两者是脱节的；想用 {@code MqMessage} 发消息必须手工拆解。
     * <p>
     * 字段映射关系：{@code destination} → channel，{@code content} → payload，
     * {@code routingKey} → 同名消息头（供 {@code topic-output} 的 routing-key-expression 取值），
     * {@code delay} → {@code x-delay} 头（直接走 {@code sendDelayed} 更省事）。
     * {@code group} 在发送侧<b>无处可放</b>：消费组由消费端 {@code bindings.<绑定名>.group} 决定，
     * 生产端指定不了，这也是它只能停在模型层的原因。
     */
    @Test
    public void testMqMessageBridge() {
        UserBean userBean = DataSource.getUser();

        // of(content, destination, group, routingKey) 四参重载
        MqMessage mqMessage = MqMessage.of(userBean, OutputMessageChannels.TOPIC_OUTPUT, "order-service", "order.created");

        // 模型自检方法
        assertTrue(mqMessage.isValid());
        assertTrue(mqMessage.hasGroup());
        assertTrue(mqMessage.hasRoutingKey());
        assertFalse(mqMessage.isDelayed(), "未调 delay → 非延迟消息");
        assertEquals(MessageType.MQ, mqMessage.getMessageType());
        assertEquals("order-service", mqMessage.getGroup());
        assertEquals("order.created", mqMessage.getRoutingKey());
        // messageId / timestamp 由 AbstractMessage 构造时自动生成，无需业务赋值
        assertNotNull(mqMessage.getMessageId());
        assertNotNull(mqMessage.getTimestamp());

        // 拆解为 MqSender 调用
        Map<String, Object> headers = Maps.of("routingKey", (Object) mqMessage.getRoutingKey()).build();
        assertTrue(mqSender.send(mqMessage.getDestination(), mqMessage.getContent(), headers));

        MqMessage delayed = MqMessage.builder()
                .content(userBean)
                .destination(OutputMessageChannels.DELAYED_OUTPUT)
                .delay(5000L)
                .build();
        assertTrue(delayed.isDelayed());
        assertEquals(Long.valueOf(5000L), delayed.getDelay());
        assertFalse(delayed.hasRoutingKey());
        assertTrue(mqSender.sendDelayed(delayed.getDestination(), delayed.getContent(), delayed.getDelay()));
    }

    /**
     * {@link MqMessage.Builder} 的必填校验：{@code build()} 会校验 content 与 destination，
     * {@code buildUnsafe()} 则完全跳过校验。
     */
    @Test
    public void testMqMessageValidation() {
        // content 缺失
        IllegalArgumentException noContent = assertThrows(IllegalArgumentException.class,
                () -> MqMessage.builder().destination(OutputMessageChannels.OUTPUT).build());
        assertEquals("消息内容不能为空", noContent.getMessage());

        // destination 缺失
        IllegalArgumentException noDestination = assertThrows(IllegalArgumentException.class,
                () -> MqMessage.builder().content(DataSource.getUser()).build());
        assertEquals("消息主题不能为空", noDestination.getMessage());

        // destination 为空白串同样不通过（内部用 trim().isEmpty() 判断）
        assertThrows(IllegalArgumentException.class,
                () -> MqMessage.builder().content(DataSource.getUser()).destination("   ").build());

        // buildUnsafe 跳过校验：能构建出来（AbstractMessage 允许 null content），但 isValid() 为 false
        MqMessage unsafe = MqMessage.builder().buildUnsafe();
        assertFalse(unsafe.isValid());
        assertFalse(unsafe.hasGroup());
        assertFalse(unsafe.hasRoutingKey());
        assertFalse(unsafe.isDelayed());
        assertEquals(MessageType.MQ, unsafe.getMessageType());
    }

    /**
     * payload 类型宽容度：{@code send} 的 payload 形参是 {@code Object}，
     * 序列化由 {@code MessageConfiguration#customMessageConverter}（基于 Jackson ObjectMapper）完成，
     * {@code message-output} 的 content-type 配的是 {@code application/json}。
     * <p>
     * <b>注意</b>：消费端 {@code messageInput} 声明的是 {@code Consumer<UserBean>}，
     * 所以发 String/Map/List 这类非 UserBean 的 payload 虽然<b>发送侧不会报错</b>，
     * 消费侧反序列化会失败并触发重试（{@code max-attempts=3}）。本用例只验证发送侧的类型宽容度。
     * <p>
     * {@code Maps.ofStr(...).and(...).build()} 返回的是 {@code Collections.unmodifiableMap}，
     * 作为 payload 或 headers 传入后不可再修改。
     */
    @Test
    public void testPayloadTypes() {
        // 1. POJO（消费端期望的类型）
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, DataSource.getUser()));

        // 2. 纯字符串 → JSON 序列化为带引号的 "hello aicyi"
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, "hello aicyi"));

        // 3. Map（链式方法是 and 不是 put）
        Map<String, Object> mapPayload = Maps.ofStr("eventName", "user.registered")
                .and("userId", 10086L)
                .build();
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, mapPayload));

        // 4. 集合 → JSON 数组
        List<UserBean> listPayload = Arrays.asList(DataSource.getUser(), DataSource.getUser());
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, listPayload));

        // 5. 数值包装类
        assertTrue(mqSender.send(OutputMessageChannels.OUTPUT, 12345L));
    }

    /**
     * topic 路由键通配符边界。
     * <p>
     * {@code topic.exchange} 有两条绑定：{@code order.#} → {@code topic.exchange.order-service}，
     * {@code #} → {@code topic.exchange.log-service}。RabbitMQ topic 规则：
     * {@code *} 恰好匹配一个单词，{@code #} 匹配<b>零个或多个</b>单词，单词以 {@code .} 分隔。
     * <p>
     * 四条路由键的预期结果（在管理台按队列 ready 增量核对）：
     * <ul>
     *     <li>{@code order} → {@code order.#} 命中（# 可匹配零个单词）+ {@code #} 命中 → 两个队列各 1 条</li>
     *     <li>{@code order.created} → 两个队列各 1 条</li>
     *     <li>{@code order.created.vip} → {@code order.#} 能匹配多级 → 两个队列各 1 条
     *         （若绑定写的是 {@code order.*} 则这条不会进 order-service）</li>
     *     <li>{@code user.login} → {@code order.#} 不命中 → <b>只有 log-service</b></li>
     * </ul>
     */
    @Test
    public void testTopicRoutingKeyBoundaries() {
        List<String> routingKeys = Arrays.asList("order", "order.created", "order.created.vip", "user.login");

        for (String routingKey : routingKeys) {
            Map<String, Object> headers = Maps.of("routingKey", (Object) routingKey).build();
            assertTrue(mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, DataSource.getUser(), headers),
                    "routingKey=" + routingKey + " 应发送成功");
        }
        log(routingKeys);
    }

    /**
     * 延迟梯度：不同 {@code x-delay} 值都能正常提交。
     * <p>
     * 延迟由 {@code delayed.exchange}（{@code x-delayed-message} 类型）实现：消息先在交换机内滞留，
     * 期满后才投递到 {@code delayed.queue}。因此在管理台上观察：发送后 ready 数<b>不会立即增加</b>，
     * 而是按各自延迟时间依次 +1；{@code delay=0} 等同不延迟。
     */
    @Test
    public void testDelayedGradient() {
        UserBean userBean = DataSource.getUser();

        for (long delayMillis : new long[]{0L, 3_000L, 10_000L}) {
            assertTrue(mqSender.sendDelayed(OutputMessageChannels.DELAYED_OUTPUT, userBean, delayMillis),
                    "delay=" + delayMillis + "ms 应发送成功");
        }
    }
}
