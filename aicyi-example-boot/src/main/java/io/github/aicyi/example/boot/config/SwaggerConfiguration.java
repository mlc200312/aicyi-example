package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.commons.util.system.SystemUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Min
 * @description springdoc-openapi（OpenAPI 3）配置，取代 springfox（不兼容 Spring Boot 3.x）
 * @date 11:59
 **/
@Configuration
public class SwaggerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${server.port:80}")
    private String serverPort;

    @Bean
    public OpenAPI aicyiOpenApi() {
        String ipAddress = SystemUtils.getIpAddress();
        // http://127.0.0.1/swagger-ui/index.html
        logger.info("Swagger url 'http://{}:{}/swagger-ui/index.html'!", ipAddress, serverPort);
        return new OpenAPI()
                .info(new Info()
                        .title("接口文档")
                        .description("接口文档示例")
                        .contact(new Contact()
                                .name("Leon Min")
                                .email("15910436675@163.com"))
                        .version("1.0"));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("io.github.aicyi.example.web")
                .build();
    }
}
