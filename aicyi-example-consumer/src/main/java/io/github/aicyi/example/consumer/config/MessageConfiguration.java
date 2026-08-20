package io.github.aicyi.example.consumer.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aicyi.example.service.channel.MessageChannels;
import org.springframework.amqp.core.*;
import org.springframework.cloud.stream.annotation.EnableBinding;
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
import java.util.HashMap;
import java.util.Map;


/**
 * @author Mr.Min
 * @description 消息配置
 * @date 15:43
 **/
@Configuration
@EnableBinding(MessageChannels.MessageInput.class)
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
}
