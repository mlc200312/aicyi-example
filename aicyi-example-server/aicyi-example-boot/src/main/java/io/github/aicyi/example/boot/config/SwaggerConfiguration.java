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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

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

    /**
     * OpenAPI 元数据（标题 / 联系人 / 安全方案）。
     * <p>
     * <b>本方法体会被执行多次，不要在此处放日志、网卡探测、远程调用等任何副作用逻辑。</b>
     * springdoc 在 {@code BeanFactoryPostProcessor} 阶段
     * （{@code SpringdocBeanFactoryConfigurer#initBeanFactoryPostProcessor}）会无条件把容器内所有
     * {@link OpenAPI} 与 {@code OpenAPIService} 的 BeanDefinition scope 改成 {@code prototype}
     * （显式标注 {@code @Scope("singleton")} 也会被其覆盖），目的是让每个 {@link GroupedOpenApi}
     * 分组拿到互不污染的独立副本 —— {@code OpenAPIService} 构造时会直接持有并原地改写该实例。
     * <p>
     * 执行次数 = 1（默认文档 {@code /v3/api-docs}）+ {@code GroupedOpenApi} 分组数：
     * 默认文档与各分组的 {@code AbstractOpenApiResource} 在构造期都会调用
     * {@code ObjectFactory#getObject()} 新建一个 {@code OpenAPIService}，进而各解析一次
     * {@code Optional<OpenAPI>}。当前 all / auth / biz 三个分组即共 4 次；请求文档端点不会再触发。
     * 启动提示因此移至 {@link #printApiDocUrls(ApplicationReadyEvent)}。
     */
    @Bean
    public OpenAPI aicyiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("接口文档")
                        .description("接口文档示例")
                        .contact(new Contact()
                                .name("Leon Min")
                                .email("mlc200312@163.com"))
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
     * 启动提示：应用就绪后打印两套文档 UI 地址，只执行一次。
     * <p>
     * 端口取自 {@link WebServer#getPort()}，为容器<b>实际监听</b>的端口，因此
     * {@code server.port=0}（随机端口）场景下也能打印正确地址；若改用
     * {@code @Value("${server.port:80}")} 读配置值，该场景下会打出 0。
     * <p>
     * 协议为 {@code http}：本工程未配置 {@code server.ssl}，80 端口是明文 HTTP，
     * 原先写 {@code https} 会导致复制出来的地址无法访问。
     */
    @EventListener
    public void printApiDocUrls(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext webContext)) {
            return;
        }
        WebServer webServer = webContext.getWebServer();
        // 非 Web 容器（如测试切片）或容器尚未启动时 getPort() 返回 -1，此时不打印
        if (webServer == null || webServer.getPort() < 0) {
            return;
        }
        int port = webServer.getPort();
        String ipAddress = SystemUtils.getIpAddress();
        logger.info("Swagger ui 'https://{}:{}/swagger-ui/index.html'!", ipAddress, port);
        logger.info("Aicyi   ui 'https://{}:{}/apidoc/index.html'!", ipAddress, port);
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
