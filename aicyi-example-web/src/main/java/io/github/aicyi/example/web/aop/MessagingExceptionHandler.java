package io.github.aicyi.example.web.aop;

import io.github.aicyi.commons.lang.model.Result;
import io.github.aicyi.example.domain.type.ExampleResultCode;
import io.github.aicyi.midware.message.core.exception.MessageSendException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = {"io.github.aicyi.example"})
public class MessagingExceptionHandler {

    @ExceptionHandler(MessageSendException.class)
    public ResponseEntity<Result<Void>> handleMessageSendException(MessageSendException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.failure(ExampleResultCode.MESSAGE_SEND_FAILURE.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MessageConversionException.class)
    public ResponseEntity<Result<Void>> handleMessageConversionException(MessageConversionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ExampleResultCode.MESSAGE_CONVERSION_FAILURE.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MessageHandlingException.class)
    public ResponseEntity<Result<Void>> handleMessageHandlingException(MessageHandlingException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ExampleResultCode.MESSAGE_HANDLING_FAILURE.getCode(), e.getMessage()));
    }
}
