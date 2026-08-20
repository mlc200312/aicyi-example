package io.github.aicyi.example.domain.type;

import io.github.aicyi.commons.lang.EnumType;
import io.github.aicyi.commons.util.jackson.EnumTypeJsonDeserializer;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Mr.Min
 * @description 性别枚举
 * @date 2020-03-02
 **/
@JsonDeserialize(using = EnumTypeJsonDeserializer.class)
public enum GenderType implements EnumType {
    MAN(1, "男"), WOMAN(2, "女");

    private final Integer code;
    private String description;

    GenderType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
