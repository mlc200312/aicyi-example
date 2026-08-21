package io.github.aicyi.example.mybatisplus;

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
@SpringBootApplication(scanBasePackages = {"io.github.aicyi.example.mybatisplus"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
@MapperScan(basePackages = {"io.github.aicyi.example.**.mapper"})
public class AicyiExampleMyBatisPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiExampleMyBatisPlusApplication.class, args);
    }
}
