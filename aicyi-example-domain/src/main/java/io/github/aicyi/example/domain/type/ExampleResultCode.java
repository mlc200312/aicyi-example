package io.github.aicyi.example.domain.type;

import io.github.aicyi.commons.lang.IResultCode;

/**
 * @author Mr.Min
 * @description 示例业务错误码枚举（遵守 3 位 HTTP 段 + 2 位序号规则）
 * @date 11:47
 **/
public enum ExampleResultCode implements IResultCode {
    OBJECT_NOT_FOUND(100001, "对象不存在"),

    MESSAGE_SEND_FAILURE(100002, "消息发送失败"),
    MESSAGE_CONVERSION_FAILURE(100002, "消息转换失败"),
    MESSAGE_HANDLING_FAILURE(100003, "消息处理失败");

    private final Integer code;
    private final String message;

    ExampleResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
