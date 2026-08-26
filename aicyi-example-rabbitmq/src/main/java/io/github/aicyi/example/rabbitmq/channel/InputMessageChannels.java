package io.github.aicyi.example.rabbitmq.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface InputMessageChannels {
    String INPUT = "message-input";
    String DELAYED_INPUT = "delayed-input";
    String DIRECT_INPUT = "direct-input";
    String ORDER_EVENTS_IN_0 = "orderEvents-in-0";
    String SYSTEM_LOGS_IN_0 = "systemLogs-in-0";

    @Input(InputMessageChannels.INPUT)
    SubscribableChannel input();

    @Input(InputMessageChannels.DELAYED_INPUT)
    SubscribableChannel delayedInput();

    @Input(InputMessageChannels.DIRECT_INPUT)
    SubscribableChannel directInput();

    @Input(InputMessageChannels.ORDER_EVENTS_IN_0)
    SubscribableChannel orderEventsIn0();

    @Input(InputMessageChannels.SYSTEM_LOGS_IN_0)
    SubscribableChannel systemLogsIn0();
}