package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Min
 * @description MapStruct 类型转换器配置（注册 commons 转换器供 spring 组件模型的 Mapper 注入）
 * @date 2026-09-02
 **/
@Configuration
public class MapStructConvertersConfiguration {

    @Bean
    public EnumTypeConverters enumTypeConverters() {
        return new EnumTypeConverters();
    }

    @Bean
    public DateTimeTypeConverters dateTimeTypeConverters() {
        return new DateTimeTypeConverters();
    }
}
