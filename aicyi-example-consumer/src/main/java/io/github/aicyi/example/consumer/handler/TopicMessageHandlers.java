package io.github.aicyi.example.consumer.handler;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.service.channel.MessageChannels;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Min
 * @description Topic 消息处理
 * @date 2025/9/25
 **/
@Component
public class TopicMessageHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @StreamListener(value = MessageChannels.ORDER_EVENTS_IN_0, condition = "headers['routingKey']=='order.created'")
    public void orderEventsCreated(org.springframework.messaging.Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
        // 处理消息逻辑
    }

    @StreamListener(value = MessageChannels.ORDER_EVENTS_IN_0, condition = "headers['routingKey']=='order.paid'")
    public void orderEventsPaid(org.springframework.messaging.Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
        // 处理消息逻辑
    }

    @StreamListener(value = MessageChannels.SYSTEM_LOGS_IN_0)
    public void systemLogs(org.springframework.messaging.Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
    }
}