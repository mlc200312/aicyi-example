package io.github.aicyi.example.rabbitmq.handler;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.example.rabbitmq.channel.InputMessageChannels;
import io.github.aicyi.example.domain.UserBean;
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

    @StreamListener(InputMessageChannels.INPUT)
    public void handleMessage(UserBean message) {
        logger.info("Received message: " + message);
        // 处理消息逻辑
    }
}