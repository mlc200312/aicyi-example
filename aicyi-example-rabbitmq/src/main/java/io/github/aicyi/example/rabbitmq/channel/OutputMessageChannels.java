package io.github.aicyi.example.rabbitmq.channel;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface OutputMessageChannels {
    String OUTPUT = "message-output";
    String DELAYED_OUTPUT = "delayed-output";
    String DIRECT_OUTPUT = "direct-output";
    String TOPIC_OUTPUT = "topic-output";

    @Output(OutputMessageChannels.OUTPUT)
    MessageChannel output();

    @Output(DELAYED_OUTPUT)
    MessageChannel delayedOutput();

    @Output(DIRECT_OUTPUT)
    MessageChannel directOutput();

    @Output(TOPIC_OUTPUT)
    MessageChannel topicOutput();
}