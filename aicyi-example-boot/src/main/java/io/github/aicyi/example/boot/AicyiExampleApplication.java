package io.github.aicyi.example.boot;

import io.github.aicyi.midware.web.annotation.EnableMidwareWeb;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Mr.Min
 * @description Spring Boot 启动类
 * @date 2023/9/7
 **/
@SpringBootApplication(scanBasePackages = {"io.github.aicyi.example"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
@EnableMidwareWeb(excludePathPatterns = {"/webjars/**", "/v2/api-docs"})
@MapperScan(basePackages = {"io.github.aicyi.example.dao.mapper"})
public class AicyiExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiExampleApplication.class, args);
    }
}
