package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.commons.util.system.SystemUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.Min
 * @description springdoc-openapi（OpenAPI 3）配置，取代 springfox（不兼容 Spring Boot 3.x）
 * <p>
 * 提供两套文档 UI 共用的后端能力：
 * <ul>
 *     <li>{@code /apidoc/index.html}：自研 layui UI（classpath:/static/apidoc/）</li>
 *     <li>{@code /swagger-ui/index.html}：springdoc 自带的官方 Swagger UI</li>
 * </ul>
 * 分组由 {@link GroupedOpenApi} 声明，UI 从 {@code /v3/api-docs/swagger-config} 的 {@code urls}
 * 读取分组列表，再按 {@code /v3/api-docs/{group}} 加载对应文档。
 * @date 11:59
 **/
@Configuration
public class SwaggerConfiguration {

    /**
     * 安全方案名称：即 {@code components.securitySchemes} 的 key，
     * UI 的 Authorize / Token 输入框按此名索引，{@link SecurityRequirement} 也引用此名
     */
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * 鉴权头名称，与 {@code AuthInterceptor} 读取的 {@code HttpHeaders.AUTHORIZATION} 一致
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${server.port:80}")
    private String serverPort;

    @Bean
    public OpenAPI aicyiOpenApi() {
        String ipAddress = SystemUtils.getIpAddress();
        logger.info("Swagger ui 'https://{}:{}/swagger-ui/index.html'!", ipAddress, serverPort);
        logger.info("Aicyi   ui 'https://{}:{}/apidoc/index.html'!", ipAddress, serverPort);
        return new OpenAPI()
                .info(new Info()
                        .title("接口文档")
                        .description("接口文档示例")
                        .contact(new Contact()
                                .name("Leon Min")
                                .email("15910436675@163.com"))
                        .version("1.0"))
                // 声明 Bearer JWT 安全方案：官方 Swagger UI 据此渲染 Authorize 按钮，
                // 自研 UI 据此渲染 Token 输入框。type=HTTP + scheme=bearer 时 in/name 不适用，不可设置
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录后获得的 accessToken，UI 发送请求时自动补全 Bearer 前缀")));
    }

    /**
     * 将 springfox 时代 controller 上手写的
     * {@code @Parameter(name = "Authorization", in = HEADER)} 转为标准 OpenAPI 3 安全声明：
     * 剔除冗余的 header 参数，改为挂 {@link SecurityRequirement}。
     * <p>
     * 仅对确实声明了该头的接口生效，因此标注 {@code @IgnoreAuth} 的授权 / 验证码接口
     * 仍保持公开（不带 security），与 {@code AuthInterceptor} 的实际行为一致。
     * 业务 controller 无需修改。
     * <p>
     * <b>必须同时注册到每个分组</b>：springdoc 只会把容器里的 {@link OperationCustomizer} Bean
     * 应用到默认文档 {@code /v3/api-docs}，<b>不会</b>自动应用到 {@link GroupedOpenApi} 分组文档；
     * 而两套 UI 都是先读 {@code /v3/api-docs/swagger-config} 再按 {@code /v3/api-docs/{group}} 加载的，
     * 若漏了分组注册，UI 实际看到的分组文档里冗余的 Authorization 头会重新出现、
     * 且丢失 security 标记（表现为需鉴权接口不被识别、调试面板多出一个手填鉴权头）。
     */
    @Bean
    public OperationCustomizer bearerSecurityOperationCustomizer() {
        return bearerSecurityCustomizer();
    }

    /**
     * 无状态实现，由上面的 {@code @Bean}（服务默认文档）与下面各分组共用，避免同一逻辑写两份
     */
    private static OperationCustomizer bearerSecurityCustomizer() {
        String inHeader = ParameterIn.HEADER.toString();
        return (operation, handlerMethod) -> {
            List<Parameter> parameters = operation.getParameters();
            if (parameters == null || parameters.isEmpty()) {
                return operation;
            }
            List<Parameter> retained = parameters.stream()
                    .filter(p -> !(AUTHORIZATION_HEADER.equalsIgnoreCase(p.getName())
                            && inHeader.equalsIgnoreCase(p.getIn())))
                    .collect(Collectors.toList());
            if (retained.size() != parameters.size()) {
                operation.setParameters(retained);
                operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            }
            return operation;
        };
    }

    /**
     * 全量分组：默认展示，覆盖整个 web 层
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("全部接口")
                .packagesToScan("io.github.aicyi.example.web")
                .addOperationCustomizer(bearerSecurityCustomizer())
                .build();
    }

    /**
     * 授权与验证码分组：{@code @IgnoreAuth} 的公开接口，调试时无需 Token
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("授权与验证码")
                .pathsToMatch("/api/auth/**", "/captcha/**")
                .addOperationCustomizer(bearerSecurityCustomizer())
                .build();
    }

    /**
     * 业务分组：需 Bearer Token 鉴权的接口
     */
    @Bean
    public GroupedOpenApi bizApi() {
        return GroupedOpenApi.builder()
                .group("biz")
                .displayName("业务接口")
                .pathsToMatch("/student/**", "/user/**")
                .addOperationCustomizer(bearerSecurityCustomizer())
                .build();
    }
}
