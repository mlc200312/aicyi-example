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
 * @description 延迟消息处理
 * @date 2025/9/25
 **/
@Component
public class DelayedMessageHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @StreamListener(MessageChannels.DELAYED_INPUT)
    public void handleMessage(org.springframework.messaging.Message<UserBean> message) {
        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("amqp_receivedRoutingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
        // 处理消息逻辑
    }
}