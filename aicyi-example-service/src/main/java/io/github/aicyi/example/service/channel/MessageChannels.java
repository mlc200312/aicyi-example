package io.github.aicyi.example.service.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MessageChannels {
    String OUTPUT = "message-output";
    String DELAYED_OUTPUT = "delayed-output";
    String DIRECT_OUTPUT = "direct-output";
    String TOPIC_OUTPUT = "topic-output";

    String INPUT = "message-input";
    String DELAYED_INPUT = "delayed-input";
    String DIRECT_INPUT = "direct-input";
    String ORDER_EVENTS_IN_0 = "orderEvents-in-0";
    String SYSTEM_LOGS_IN_0 = "systemLogs-in-0";

    /**
     * @author Mr.Min
     * @description 生产者
     * @date 2025/9/25
     **/
    interface MessageOutput {

        @Output(MessageChannels.OUTPUT)
        MessageChannel output();

        @Output(DELAYED_OUTPUT)
        MessageChannel delayedOutput();

        @Output(DIRECT_OUTPUT)
        MessageChannel directOutput();

        @Output(TOPIC_OUTPUT)
        MessageChannel topicOutput();
    }

    /**
     * @author Mr.Min
     * @description 消费者
     * @date 2025/9/25
     **/
    interface MessageInput {

        @Input(MessageChannels.INPUT)
        SubscribableChannel input();

        @Input(MessageChannels.DELAYED_INPUT)
        SubscribableChannel delayedInput();

        @Input(MessageChannels.DIRECT_INPUT)
        SubscribableChannel directInput();

        @Input(MessageChannels.ORDER_EVENTS_IN_0)
        SubscribableChannel orderEventsIn0();

        @Input(MessageChannels.SYSTEM_LOGS_IN_0)
        SubscribableChannel systemLogsIn0();
    }
}