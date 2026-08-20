package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.util.orikamapper.OrikaMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:09
 **/
@Configuration
public class ManagerConfiguration {

    @Bean
    public BeanMapper smartMapper() {
        return new OrikaMapper();
    }
}
