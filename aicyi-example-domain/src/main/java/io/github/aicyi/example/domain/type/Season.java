package io.github.aicyi.example.domain.type;

import io.github.aicyi.commons.lang.StringEnumType;
import io.github.aicyi.commons.util.jackson.StringEnumTypeJsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Mr.Min
 * @description 季节枚举
 * @date 2019-05-21
 **/
@JsonDeserialize(using = StringEnumTypeJsonDeserializer.class)
public enum Season implements StringEnumType {
    SPRING("春", "春天"),
    SUMMER("夏", "夏天"),
    AUTUMN("秋", "秋天"),
    WINTER("冬", "冬天"),
    ;

    private String code;
    private String description;

    Season(String code, String description) {
        this.code = code;
        this.description = description;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
