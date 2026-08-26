package io.github.aicyi.example.rabbitmq;

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
public class AicyiExampleRabbitmqApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiExampleRabbitmqApplication.class, args);
    }
}
