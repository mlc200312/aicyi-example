package io.github.aicyi.example.rabbitmq.channel;

/**
 * 输出通道常量（Spring Cloud Stream 4.x 函数式模型）。
 * <p>
 * 4.x 移除了 @Output 注解式编程，生产者统一通过 StreamBridge / MqSender 发送，
 * 发送目标为绑定名（与 spring.cloud.stream.bindings 中的 key 一致）。
 */
public interface OutputMessageChannels {
    String OUTPUT = "message-output";
    String DELAYED_OUTPUT = "delayed-output";
    String DIRECT_OUTPUT = "direct-output";
    String TOPIC_OUTPUT = "topic-output";
}
