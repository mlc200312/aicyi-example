package io.github.aicyi.example.rabbitmq.channel;

/**
 * 输入通道常量（Spring Cloud Stream 4.x 函数式模型）。
 * <p>
 * 4.x 移除了 @EnableBinding/@Input/@StreamListener 注解式编程，消费者改为
 * {@code Consumer<?>} 函数 Bean，绑定名自动派生为 {@code <函数名>-in-0}，
 * 函数名需在 spring.cloud.stream.function.definition 中声明（见 nacos/aicyi-example-rabbitmq.yml）。
 */
public interface InputMessageChannels {
    String INPUT = "messageInput";
    String DELAYED_INPUT = "delayedInput";
    String DIRECT_INPUT = "directInput";
    String ORDER_EVENTS_IN_0 = "orderEvents-in-0";
    String SYSTEM_LOGS_IN_0 = "systemLogs-in-0";
}
