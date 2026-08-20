package io.github.aicyi.example.boot.config;

import io.github.aicyi.example.service.channel.MessageChannels;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Min
 * @description MQ消息相关配置
 * @date 15:43
 **/
@Configuration
@EnableBinding(MessageChannels.MessageOutput.class)
public class MessageConfiguration {
}
