package io.github.aicyi.example.consumer;

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
@MapperScan(basePackages = {"io.github.aicyi.example.dao.mapper"})
public class AicyiExampleConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiExampleConsumerApplication.class, args);
    }
}
