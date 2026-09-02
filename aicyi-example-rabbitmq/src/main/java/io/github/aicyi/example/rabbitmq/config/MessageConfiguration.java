package io.github.aicyi.example.rabbitmq.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.rabbitmq.handler.DelayedMessageHandlers;
import io.github.aicyi.example.rabbitmq.handler.DirectMessageHandlers;
import io.github.aicyi.example.rabbitmq.handler.MessageHandlers;
import io.github.aicyi.example.rabbitmq.handler.TopicMessageHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.function.Consumer;


/**
 * @author Mr.Min
 * @description 消息配置
 * <p>
 * Spring Cloud Stream 4.x 函数式编程模型：以 Consumer Bean 声明消费者，
 * Bean 名即函数名，绑定名自动派生为 {@code <函数名>-in-0}；
 * 取代旧版 @EnableBinding/@StreamListener 注解式模型（4.x 已移除）。
 * @date 15:43
 **/
@Configuration
public class MessageConfiguration {

    @Bean
    public MessageConverter customMessageConverter(ObjectMapper objectMapper) {
        return new AbstractMessageConverter(MimeTypeUtils.APPLICATION_JSON) {
            @Override
            protected boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            protected Object convertFromInternal(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {
                byte[] payload = (byte[]) message.getPayload();
                try {
                    return objectMapper.readValue(payload, targetClass);
                } catch (IOException e) {
                    throw new MessageConversionException("Failed to convert message", e);
                }
            }

            @Override
            protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
                try {
                    return objectMapper.writeValueAsBytes(payload);
                } catch (JsonProcessingException e) {
                    throw new MessageConversionException("Failed to serialize message", e);
                }
            }
        };
    }

    /**
     * 默认消息消费者，绑定 messageInput-in-0
     */
    @Bean
    public Consumer<UserBean> messageInput(MessageHandlers handlers) {
        return handlers::handleMessage;
    }

    /**
     * 延迟消息消费者，绑定 delayedInput-in-0
     */
    @Bean
    public Consumer<Message<UserBean>> delayedInput(DelayedMessageHandlers handlers) {
        return handlers::handleMessage;
    }

    /**
     * Direct 消息消费者，绑定 directInput-in-0
     */
    @Bean
    public Consumer<Message<UserBean>> directInput(DirectMessageHandlers handlers) {
        return handlers::handleMessage;
    }

    /**
     * 订单事件消费者，绑定 orderEvents-in-0。
     * 旧版 @StreamListener 的 condition SpEL 路由在函数式模型中由方法内分发替代。
     */
    @Bean
    public Consumer<Message<UserBean>> orderEvents(TopicMessageHandlers handlers) {
        return handlers::handleOrderEvent;
    }

    /**
     * 系统日志消费者，绑定 systemLogs-in-0
     */
    @Bean
    public Consumer<Message<UserBean>> systemLogs(TopicMessageHandlers handlers) {
        return handlers::systemLogs;
    }
}
