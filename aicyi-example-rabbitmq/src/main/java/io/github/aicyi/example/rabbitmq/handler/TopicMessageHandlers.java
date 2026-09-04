package io.github.aicyi.example.rabbitmq.handler;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.lang.exception.BusinessException;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.example.domain.bo.UserBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Min
 * @description Topic 消息处理（函数 orderEvents / systemLogs，绑定 orderEvents-in-0 / systemLogs-in-0）。
 * <p>
 * 旧版 @StreamListener 的 condition SpEL 条件路由在 Spring Cloud Stream 4.x
 * 函数式模型中不再支持，改为在方法内按 routingKey 头分发。
 * @date 2025/9/25
 **/
@Component
public class TopicMessageHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 订单事件：按 routingKey 分发（替代原 @StreamListener condition）
     */
    public void handleOrderEvent(Message<UserBean> message) {
        MessageHeaders headers = message.getHeaders();
        Object routingKey = headers.get("routingKey");

        if ("order.created".equals(routingKey)) {
            orderEventsCreated(message);
        } else if ("order.paid".equals(routingKey)) {
            orderEventsPaid(message);
        } else {

            logger.info("Ignored order event [{}]: {}", routingKey, message.getPayload());

            throw new BusinessException("Unknown routing key: " + routingKey);
        }
    }

    public void orderEventsCreated(Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
        // 处理消息逻辑
    }

    public void orderEventsPaid(Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
        // 处理消息逻辑
    }

    public void systemLogs(Message<UserBean> message) {

        MessageHeaders headers = message.getHeaders();

        Object object = headers.get("routingKey");

        logger.info("Received message [{}]: {}", object, message.getPayload());
    }
}
