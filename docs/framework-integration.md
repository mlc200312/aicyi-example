# 将 aicyi 集成到自有项目

aicyi 是一套可独立引用的 Spring Boot 2.7 基础框架（BOM / commons / midware），
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

    <!-- 中间件自动装配（Redis/Snowflake/MyBatis-Plus） -->
    <dependency>
        <groupId>io.github.aicyi.midware</groupId>
        <artifactId>aicyi-midware-spring-boot-starter</artifactId>
    </dependency>

    <!-- Redis 增强模板/锁（aicyi.redis.enabled=true 时生效） -->
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
@EnableMidwareWeb            // 启用统一响应、全局异常处理、鉴权、请求日志与链路追踪
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
  mvc:
    path-match:
      matching-strategy: ant_path_matcher   # springfox 3.0 兼容必需项

aicyi:
  redis:
    enabled: true                  # 开启 Redis 增强模板
  snowflake:
    enabled: true                  # 开启分布式 ID
    service-name: your-service     # 多服务共用同一 Redis 时必须各不相同
  mybatis-plus:
    enabled: true                  # 缺省即开启，可省略
```

## 常见问题

### Q: 启动报 `documentationPluginsBootstrapper` NPE？

springfox 3.0 与 Spring Boot 2.6+ 路径匹配策略不兼容，配置 `spring.mvc.path-match.matching-strategy: ant_path_matcher`。

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
