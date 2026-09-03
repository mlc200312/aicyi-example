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
 * <p>
 * {@code excludePathPatterns} 需同时放行两套文档 UI 与其数据端点：
 * <ul>
 *     <li>{@code /apidoc/**}、{@code /api-doc.html}：自研 layui 文档 UI（已从 {@code /webjars/swagger-ui/} 迁出）</li>
 *     <li>{@code /swagger-ui/**}、{@code /webjars/**}：springdoc 自带的官方 Swagger UI 及其 webjars 资源</li>
 *     <li>{@code /v3/api-docs/**}：springdoc 的 OpenAPI 数据端点（含 {@code /v3/api-docs} 本身与 {@code /swagger-config}）</li>
 * </ul>
 * 旧值 {@code /v2/api-docs} 是 springfox 时代的端点，springdoc 2.x 已改为 {@code /v3/api-docs}，
 * 保留旧值会导致文档端点被鉴权拦截器拦下返回 401。
 * @date 2023/9/7
 **/
@SpringBootApplication(scanBasePackages = {"io.github.aicyi.example"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
@EnableMidwareWeb(excludePathPatterns = {
        "/apidoc/**", "/api-doc.html",
        "/swagger-ui/**", "/webjars/**",
        "/v3/api-docs/**"
})
@MapperScan(basePackages = {"io.github.aicyi.example.dao.mapper"})
public class AicyiExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicyiExampleApplication.class, args);
    }
}
