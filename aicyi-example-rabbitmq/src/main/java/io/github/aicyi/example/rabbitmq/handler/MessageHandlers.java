package io.github.aicyi.example.rabbitmq.handler;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.example.domain.UserBean;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Min
 * @description 默认消息处理（函数 messageInput，绑定 messageInput-in-0）
 * @date 2025/9/25
 **/
@Component
public class MessageHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void handleMessage(UserBean message) {
        logger.info("Received message: " + message);
        // 处理消息逻辑
    }
}
