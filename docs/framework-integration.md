# 将 aicyi 集成到自有项目

aicyi 是一套可独立引用的 Spring Boot 3.2 / JDK 17 基础框架（BOM / commons / midware），
本文说明如何在自己的 Maven 工程中接入。框架完整能力见 aicyi 仓库的 `docs/`。

> 前提：已从 aicyi 仓库执行 `mvn clean install -DskipTests`，将基础包安装到本地（或内部）仓库。

## 步骤 1：引入 Parent POM（统一版本与插件管理）

```xml
<parent>
    <groupId>io.github.aicyi.base</groupId>
    <artifactId>aicyi-base-starter-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
```

## 步骤 2：引入所需模块

`aicyi-midware-spring-boot-starter` 提供 Redis / Snowflake / MyBatis-Plus 自动配置
（其中 redis、db-mybatisplus 为 provided+optional，需自行引入）；
`@EnableMidwareWeb` 位于 `aicyi-midware-web`，同样需显式引入。

```xml
<dependencies>
    <!-- 核心公共库 -->
    <dependency>
        <groupId>io.github.aicyi.commons</groupId>
        <artifactId>aicyi-commons-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.aicyi.commons</groupId>
        <artifactId>aicyi-commons-lang</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.aicyi.commons</groupId>
        <artifactId>aicyi-commons-util</artifactId>
    </dependency>

    <!-- Web 能力：@EnableMidwareWeb（统一响应/异常/鉴权/请求日志/链路追踪） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-web</artifactId>
    </dependency>

    <!-- 中间件自动装配（RabbitMq/Redis/Snowflake/MyBatis-Plus） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-spring-boot-starter</artifactId>
    </dependency>

    <!-- rabbitmq 增强（aicyi.mq.rabbitmq.enabled=true 缺省开启） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-rabbitmq</artifactId>
    </dependency>

    <!-- Redis 增强模板/锁（aicyi.redis.enabled=true 缺省开启） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-redis</artifactId>
    </dependency>

    <!-- MyBatis-Plus 增强（aicyi.mybatis-plus.enabled 缺省开启） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-db-mybatisplus</artifactId>
    </dependency>

    <!-- 消息系统（可选：邮件/短信/MQ） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-message-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## 步骤 3：启用框架功能

在启动类添加注解：

```java
@SpringBootApplication
// 启用统一响应、全局异常处理、鉴权、请求日志与链路追踪；
// excludePathPatterns 需放行接口文档 UI 与其数据端点，否则鉴权拦截器会把文档请求转成错误信封
@EnableMidwareWeb(excludePathPatterns = {
        "/apidoc/**", "/api-doc.html",      // 自研 layui UI（classpath:/static/apidoc/）与跳板页
        "/swagger-ui/**", "/webjars/**",    // springdoc 自带的官方 Swagger UI 及其 webjars 资源
        "/v3/api-docs/**"                   // OpenAPI 数据端点（springdoc 2.x；springfox 时代为 /v2/api-docs）
})
@MapperScan("com.your.dao")  // 扫描 MyBatis Mapper
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

> 鉴权默认开启（`enableAuth = true`），此时容器中必须存在 `AuthenticationTokenService` Bean，否则启动即失败。
> 定义 Token 服务示例（基于 Redis 刷新 Token）：

```java
@Configuration
public class WebConfiguration {

    @Bean
    public AuthenticationTokenService<IJWTInfo> authenticationTokenService(
            EnhancedRedisTemplateFactory templateFactory) {
        AuthenticationConfig config = AuthenticationConfig.builder()
                .secretKey("your-secret-key")
                .issuer("your-app")
                .subject("your-app.com")
                .refreshTokenTtl(7).refreshTokenTimeUnit(TimeUnit.DAYS)
                .accessTokenTtl(1).accessTokenTimeUnit(TimeUnit.DAYS)
                .multiTokenAllowed(true).multiTokenCount(2)
                .build();
        return new JwtRefreshAuthenticationTokenService<>(
                config, templateFactory.getStringRedisTemplate(), YourUserInfo.class);
    }
}
```

`YourUserInfo` 需实现 `IJWTInfo`（`getId`/`getUniqueName`/`getDeviceId`）。

## 步骤 4：配置 application.yml

> 示例工程把下列 `springdoc` 配置放在 `aicyi-example-boot/src/main/resources/application-test.yml`
>（环境无关但与 profile 一同维护），放到 `application.yml` 同样生效。

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aicyi?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379

aicyi:
  redis:
    enabled: true                  # 开启 Redis 增强模板
  snowflake:
    enabled: true                  # 开启分布式 ID
    service-name: your-service     # 多服务共用同一 Redis 时必须各不相同
  mybatis-plus:
    enabled: true                  # 缺省即开启，可省略

springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
    # 固定输出 OpenAPI 3.0.x：3.1 改用 type 数组并移除 nullable 来表达可空，
    # 与自研 UI（/apidoc）及多数下游客户端的 3.0 解析语义不兼容
    version: openapi_3_0
    groups:
      enabled: true                # 开启 GroupedOpenApi 分组端点 /v3/api-docs/{group}
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    disable-swagger-default-url: true   # 禁用默认的 Petstore 示例接口
    groups-order: asc              # 分组、tag、接口均按字母序，避免每次启动顺序抖动
    tags-sorter: alpha
    operations-sorter: alpha
  cache:
    disabled: false                # 多分组下可显著降低重复扫描开销
```

## 常见问题

### Q: 接口文档页面打开是一段 JSON 错误体？

两个常见原因：

1. **`excludePathPatterns` 未放行**。见步骤 3，注意 springdoc 2.x 的端点是 `/v3/api-docs`，
   沿用 springfox 时代的 `/v2/api-docs` 会导致文档端点被鉴权拦下。
2. **自定义了 `/webjars/**` 的 `ResourceHandler`**。Boot 默认把 `/webjars/**` 映射到
   `classpath:/META-INF/resources/webjars/`，自行改成 `classpath:/webjars/` 会覆盖它，
   使 springdoc 依赖的 `org.webjars:swagger-ui` 资源无法按 webjars 路径访问。
   自研 UI 已放在 `classpath:/static/apidoc/`，由 Boot 默认静态资源映射直接暴露，**无需任何自定义映射**。

因为全局异常处理器会把资源解析异常转成 HTTP 200 + JSON 错误体，前端表现为「加载成功但内容错乱」，
排查时不要只看状态码。

### Q: controller 上手写的 `@Parameter(name = "Authorization", in = HEADER)` 还需要吗？

不需要。推荐在 `OpenAPI` Bean 里声明 `securitySchemes`（`type=HTTP` + `scheme=bearer`），
再用一个 `OperationCustomizer` 把旧注解产生的冗余 header 参数剔除并改挂 `SecurityRequirement`，
业务 controller 可以零改动。只对确实声明过该头的接口生效，因此标了 `@IgnoreAuth`
的公开接口仍保持无 `security`，与鉴权拦截器的实际行为一致。完整实现见
`aicyi-example-boot` 的 `SwaggerConfiguration`。

**陷阱：`OperationCustomizer` Bean 不会自动作用于分组文档。**
springdoc 只把容器里的 `OperationCustomizer` Bean 应用到默认文档 `/v3/api-docs`，
**不会**应用到 `GroupedOpenApi` 声明的 `/v3/api-docs/{group}`；而两套 UI 都是先读
`/v3/api-docs/swagger-config` 再按分组端点加载的，于是 UI 上看到的仍是未定制的文档
（冗余的 Authorization 头重新出现、`security` 标记丢失，表现为需鉴权接口不被识别）。
必须把 customizer 抽成可复用的无状态实例，在**每个** `GroupedOpenApi.builder()` 上显式注册：

```java
/** 无状态实现，由 @Bean（服务默认文档）与各分组共用，避免同一逻辑写两份 */
private static OperationCustomizer bearerSecurityCustomizer() {
    return (operation, handlerMethod) -> { /* 剔除 Authorization 头 + 挂 SecurityRequirement */ };
}

@Bean
public OperationCustomizer bearerSecurityOperationCustomizer() {
    return bearerSecurityCustomizer();          // 仅对 /v3/api-docs 生效
}

@Bean
public GroupedOpenApi bizApi() {
    return GroupedOpenApi.builder()
            .group("biz")
            .pathsToMatch("/student/**", "/user/**")
            .addOperationCustomizer(bearerSecurityCustomizer())   // 分组文档必须显式注册
            .build();
}
```

> 验证方式：分别拉取 `/v3/api-docs` 与 `/v3/api-docs/{group}`，比对两者中 `security` 的出现次数
> 与残留的 `"name":"Authorization"` 参数个数，**两端都应生效**；只查默认端点会漏判。
> 注意 springdoc 输出的 JSON 无空格，检索时不要写成 `"name": "Authorization"`。

### Q: 如何关闭 Snowflake / Redis？

```yaml
aicyi:
  snowflake:
    enabled: false
  redis:
    enabled: false
```

### Q: 如何跳过特定接口的认证？

在 Controller 类或方法上添加 `@IgnoreAuth`；关闭整个应用的鉴权使用 `@EnableMidwareWeb(enableAuth = false)`。

### Q: 如何引入日志模板？

在自己的 `logback-spring.xml` 中显式引入基础包日志片段：

```xml
<include resource="aicyi/logback-aicyi-defaults.xml"/>
```
