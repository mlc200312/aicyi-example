package io.github.aicyi.example.consumer.handler;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.service.channel.MessageChannels;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Min
 * @description 默认消息处理
 * @date 2025/9/25
 **/
@Component
public class MessageHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @StreamListener(MessageChannels.INPUT)
    public void handleMessage(UserBean message) {
        logger.info("Received message: " + message);
        // 处理消息逻辑
    }
}