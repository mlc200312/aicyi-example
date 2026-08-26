package io.github.aicyi.example.rabbitmq;

import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.example.fixture.util.DataSource;
import io.github.aicyi.example.rabbitmq.channel.OutputMessageChannels;
import io.github.aicyi.midware.message.mq.sender.MqSender;
import io.jsonwebtoken.lang.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 20:30
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleRabbitmqApplication.class)
public class MqSenderTest extends BaseLoggerTest {

    @Autowired(required = false)
    @Qualifier(OutputMessageChannels.OUTPUT)
    private MessageChannel messageChannel;
    @Autowired(required = false)
    @Qualifier(OutputMessageChannels.TOPIC_OUTPUT)
    private MessageChannel topicMessageChannel;
    @Autowired(required = false)
    @Qualifier(OutputMessageChannels.DIRECT_OUTPUT)
    private MessageChannel directMessageChannel;
    @Autowired(required = false)
    @Qualifier(OutputMessageChannels.DELAYED_OUTPUT)
    private MessageChannel delayedMessageChannel;

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

        // 方式一
        messageChannel.send(message);
        topicMessageChannel.send(message);
        directMessageChannel.send(message);

        // 方式二
        mqSender.send(OutputMessageChannels.OUTPUT, userBean);
        mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean);
        mqSender.send(OutputMessageChannels.DIRECT_OUTPUT, userBean);
    }

    @Test
    public void test2() {
        UserBean userBean = DataSource.getUser();

        Map<String, Object> headers = Maps.of("routingKey", (Object) "order.created").build();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).copyHeaders(headers).build();

        // 方式一
        messageChannel.send(message);
        topicMessageChannel.send(message);
        directMessageChannel.send(message);

        // 方式二
        mqSender.send(OutputMessageChannels.OUTPUT, userBean, headers);
        mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers);
        mqSender.send(OutputMessageChannels.DIRECT_OUTPUT, userBean, headers);
    }

    @Test
    public void test3() {
        UserBean userBean = DataSource.getUser();

        long delayMillis = 10 * 1000;

        Map<String, Object> headers = Maps.of("x-delay", (Object) delayMillis).build();

        Message<UserBean> message = MessageBuilder.withPayload(userBean).copyHeaders(headers).build();
        // 方式一
        delayedMessageChannel.send(message, delayMillis);

        // 方式二
        // 立即发送
        mqSender.send(OutputMessageChannels.DELAYED_OUTPUT, userBean);
        // 延迟发送
        mqSender.sendDelayed(OutputMessageChannels.DELAYED_OUTPUT, userBean, delayMillis);
    }

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
        topicMessageChannel.send(message);
        topicMessageChannel.send(message2);
        topicMessageChannel.send(message3);

        // 方式二
        mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers);
        mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers2);
        mqSender.send(OutputMessageChannels.TOPIC_OUTPUT, userBean, headers3);
    }
}
