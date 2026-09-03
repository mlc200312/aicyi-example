# aicyi-example-boot（主应用）

主 Web 应用，聚合了 aicyi 框架的绝大部分能力，是了解框架的最佳入口。

- 端口：**80**
- 启动类：`io.github.aicyi.example.boot.AicyiExampleApplication`

## 启动

```bash
cd aicyi-example-boot
mvn spring-boot:run
```

前置条件：MySQL、Redis、Nacos 已就绪，Nacos 已导入配置（见 [Nacos 配置中心](../infra/nacos.md)），
数据库已初始化（见 [快速开始 - 第 2 节](../quickstart.md#2-初始化数据库)）。

## 配置

### 本地 `application.yml`

- `spring.profiles.active: test` → 加载 `application-test.yml`
- `spring.cloud.nacos.config`：Nacos 地址与账号（可被环境变量覆盖）
- `server.port: 80`
- `logging.config: classpath:logback-spring.xml`

> 已不再配置 `spring.mvc.path-match.matching-strategy: ant_path_matcher`：那是 springfox 3.0 的兼容必需项，
> springdoc-openapi 2.x 不依赖它，保留反而会关掉 Boot 3 默认的 `PathPatternParser`。

### Nacos 导入清单（`application-test.yml`）

| Data ID | 作用 |
| --- | --- |
| `aicyi-example.yml` | 共享：MyBatis 日志实现、freemarker |
| `aicyi-example-test.yml` | test 环境：snowflake、message（邮箱/短信/MQ） |
| `aicyi-datasource.yml` | 数据源（MySQL `test` 库） |
| `aicyi-redis.yml` | Redis 连接 |
| `aicyi-rabbitmq.yml` | RabbitMQ 连接与 Stream binder |

## 核心配置类（`boot/config` 包）

| 配置类 | 职责 |
| --- | --- |
| `WebConfiguration` | JWT Token 服务（`JwtRefreshAuthenticationTokenService`）、CORS |
| `RedisConfiguration` | `RedissonClient`、`Cache<String,String>`、`Cache<String,UserInfo>`（RedisCache） |
| `MapStructConvertersConfiguration` | `EnumTypeConverters`、`DateTimeTypeConverters`（MapStruct 类型转换器） |
| `PasswordEncoderConfiguration` | `BCryptPasswordEncoder` |
| `DataSourceConfiguration` | 事务管理器、枚举 TypeHandler 自动注册（Reflections） |
| `SwaggerConfiguration` | springdoc-openapi 文档：`securitySchemes`（Bearer JWT）、`OperationCustomizer`、`all`/`auth`/`biz` 三个分组 |

> `WebConfiguration` 已移除自定义的 `addResourceHandlers`：自研文档 UI 迁至 `classpath:/static/apidoc/`、
> 跳板页迁至 `classpath:/static/api-doc.html`，均由 Spring Boot 默认静态资源映射暴露。
> 原先的 `/webjars/** -> classpath:/webjars/` 映射会覆盖 Boot 默认的
> `/webjars/** -> classpath:/META-INF/resources/webjars/`，使 springdoc 自带的 `org.webjars:swagger-ui`
> 资源无法按 webjars 路径访问（异常被全局处理器转成 HTTP 200 + JSON 错误体，前端表现为「加载成功但内容错乱」）。

## 接口文档 UI

由 springdoc-openapi 2.5.0 输出的 OpenAPI 3.0 文档驱动，两套 UI 并存：

| 入口 | 说明 |
| --- | --- |
| `/apidoc/index.html` | 自研 layui UI（推荐）：分组切换、Token 调试、Mock 示例、参数展平 |
| `/api-doc.html` | 跳板页，转向自研 UI（保留 `#` 与查询串，兼容旧书签） |
| `/swagger-ui/index.html` | springdoc 自带的官方 Swagger UI |

数据端点：`/v3/api-docs`（全量）、`/v3/api-docs/swagger-config`（分组清单）、`/v3/api-docs/{group}`。

| 分组 | 范围 | 鉴权 |
| --- | --- | --- |
| `all` | `io.github.aicyi.example.web` 全包 | 混合 |
| `auth` | `/api/auth/**`、`/captcha/**` | `@IgnoreAuth` 公开，调试无需 Token |
| `biz` | `/student/**`、`/user/**` | 需 Bearer Token |

鉴权声明：`SwaggerConfiguration` 在 `components.securitySchemes` 里声明 `bearerAuth`
（`type=HTTP` + `scheme=bearer` + `bearerFormat=JWT`），并用 `bearerSecurityOperationCustomizer`
把 controller 上手写的 `@Parameter(name = "Authorization", in = HEADER)` 剔除、改挂 `SecurityRequirement`，
**业务 controller 零改动**。只对确实声明过该头的接口生效，因此 `@IgnoreAuth` 的公开接口仍不带 `security`，
与 `AuthInterceptor` 的实际行为一致。

调试流程：调 `/api/auth/login` 取 `data.token.accessToken` → 填入自研 UI 顶部的 Token 输入框 →
后续请求自动补全 `Authorization: Bearer <token>`（缺 `Bearer ` 前缀会被拒，返回 `code=40101`）。

## 接口清单

统一响应格式 `Result<D>`（`code`/`message`/`data`/`traceId`），需认证接口携带
`Authorization: Bearer <accessToken>`。

### 认证 `/api/auth`（`@IgnoreAuth` 免认证）

带 `*` 的为必填（Bean Validation）：

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | `username`*, `password`*, `mobile`*, `birthday?`, `genderType?` | 注册（不校验验证码，无密码强度要求） |
| POST | `/api/auth/login` | `username`*, `password`*, `uuid`*, `verCode`* | 登录，返回 `userId` + `token` |
| POST | `/api/auth/refresh-token` | `refreshToken`* | 刷新 AccessToken |
| POST | `/api/auth/update-password` | `username`*, `newPassword`*, `uuid`*, `verCode`* | 更新密码 |

### 验证码 `/captcha`（`@IgnoreAuth` 免认证）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| GET | `/captcha/get-captcha` | - | 生成图形验证码，返回 `uuid` 与图片地址 |
| GET | `/captcha/{uuid}` | - | 输出验证码图片（实际字节为 PNG） |
| POST | `/captcha/send-email-captcha` | `captchaType`*, `username`*, `uuid`*, `verCode`* | 向用户邮箱发验证码 |
| POST | `/captcha/send-sms-captcha` | `captchaType`*, `username`*, `uuid`*, `verCode`* | 向用户手机发验证码 |

> `captchaType` 为整型（`@NotNull`），取值见 `CaptchaType` 枚举。

### 用户 `/user`（需认证）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| GET | `/user/get-user-info` | - | 获取当前登录用户信息（从上下文取） |
| POST | `/user/update-user-info` | `mobile`, `email`, `nickname`, `idCard`, `age`, `genderType`, `birthday` | 更新当前用户 |

### 学生 `/student`（需认证）

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/student/get-by-id` | `id`* | 按 ID 查询 |
| GET | `/student/get-by-mobile` | `mobile`* | 按手机号查询 |
| GET | `/student/paged-list` | 见下表 | 分页查询（PageHelper） |
| POST | `/student/add-student` | `idCard`*, `gradeType`* | 新增学生（请求体仅此两个字段，均 `@NotBlank`） |

`/student/paged-list` 以 `@Validated @ModelAttribute StudentReq` 接收，参数按**扁平**方式拼在 query string 上：

| 参数 | 必填 | 约束 |
| --- | --- | --- |
| `page` | **是** | `@NotNull`（继承自 `PageRequest`） |
| `size` | **是** | `@NotNull` + `@Max(500)` |
| `registerTimeStart` | **是** | `@NotBlank` + `@Pattern`，格式 `yyyy-MM-dd HH:mm:ss.SSS` |
| `registerTimeEnd` | **是** | 同上 |
| `userIdEq` | 否 | 用户 ID 精确匹配 |
| `gradeTypeEq` | 否 | 班级精确匹配 |

> 四个必填项缺任意一个都会返回 `code=40001`（如 `registerTimeStart:不能为空`）。
> 时间参数含空格与冒号，需 URL 编码：`registerTimeStart=2020-01-01%2000%3A00%3A00.000`。
>
> **不要写成嵌套形式** `?req.page=1&req.size=10`：Spring MVC 对 `@ModelAttribute` 对象参数按扁平名绑定，
> 加 `req.` 前缀会导致整个对象绑定不上（实测返回 `size:不能为null,page:不能为null`）。
> 自研文档 UI 已把 springdoc 输出的 `req` 对象参数自动展平，调试时无需关心。

```bash
# 分页查询示例（注意时间参数的 URL 编码）
curl "http://localhost/student/paged-list?page=1&size=10&registerTimeStart=2020-01-01%2000%3A00%3A00.000&registerTimeEnd=2030-12-31%2023%3A59%3A59.999" \
  -H "Authorization: Bearer <accessToken>"
```

### 示例：注册 → 登录 → 认证访问

```bash
# 注册
curl -X POST http://localhost/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456","mobile":"13800138001"}'

# 登录（test 环境 verCode 任意）
curl -X POST http://localhost/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456","uuid":"any","verCode":"any"}'

# 带 token 访问
curl http://localhost/user/get-user-info -H "Authorization: Bearer <accessToken>"
```

## 测试

`src/test/java` 下覆盖框架核心能力（需本机 Redis/MySQL 可用）：

| 分组 | 测试 |
| --- | --- |
| redis | `RedisCacheTest`、`EnhancedRedisTemplateFactoryTest`、`DistributedLockTest`、`RedisSnowflakeIdGeneratorTest`、`RedisTokenServiceImplTest`、`MultiRedisTokenServiceImplTest` |
| token | `AuthenticationTokenServiceTest`、`JwtRefreshAuthenticationTokenServiceTest` |
| message | `UnifiedMessageManagerTest`、`MailSenderTest`、`SmsSenderTest` |
| 其他 | `ServiceTest`、`LoggerTest` |

```bash
mvn test -pl aicyi-example-boot
```
