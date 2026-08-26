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
- `spring.mvc.path-match.matching-strategy: ant_path_matcher`：兼容 springfox 3.0 的必需项
- `server.port: 80`

### Nacos 导入清单（`application-test.yml`）

| Data ID | 作用 |
| --- | --- |
| `aicyi-example.yml` | 共享：MyBatis 日志、mvc 路径匹配、freemarker |
| `aicyi-example-test.yml` | test 环境：snowflake、message（邮箱/短信/MQ） |
| `aicyi-datasource.yml` | 数据源（MySQL `test` 库） |
| `aicyi-redis.yml` | Redis 连接 |
| `aicyi-rabbitmq.yml` | RabbitMQ 连接与 Stream binder |

## 核心配置类（`boot/config` 包）

| 配置类 | 职责 |
| --- | --- |
| `WebConfiguration` | JWT Token 服务（`JwtRefreshAuthenticationTokenService`）、CORS、静态资源映射 |
| `RedisConfiguration` | `RedissonClient`、`Cache<String,String>`、`Cache<String,UserInfo>`（RedisCache） |
| `ManagerConfiguration` | `BeanMapper`（Orika 实现） |
| `PasswordEncoderConfiguration` | `BCryptPasswordEncoder` |
| `DataSourceConfiguration` | 事务管理器、枚举 TypeHandler 自动注册（Reflections） |
| `SwaggerConfiguration` | springfox OpenAPI 3 文档（`/api-doc.html`） |

## 接口清单

统一响应格式 `Result<D>`（`code`/`message`/`data`/`traceId`），需认证接口携带
`Authorization: Bearer <accessToken>`。

### 认证 `/api/auth`（`@IgnoreAuth` 免认证）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | `username`, `password`, `mobile`, `birthday?`, `genderType?` | 注册 |
| POST | `/api/auth/login` | `username`, `password`, `uuid`, `verCode` | 登录，返回 `userId` + `token` |
| POST | `/api/auth/refresh-token` | `refreshToken` | 刷新 AccessToken |
| POST | `/api/auth/update-password` | `username`, `newPassword`, `uuid`, `verCode` | 更新密码 |

### 验证码 `/captcha`（`@IgnoreAuth` 免认证）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| GET | `/captcha/get-captcha` | - | 生成图形验证码，返回 `uuid` 与图片地址 |
| GET | `/captcha/{uuid}` | - | 输出验证码图片（JPEG） |
| POST | `/captcha/send-email-captcha` | `captchaType`, `username`, `uuid`, `verCode` | 向用户邮箱发验证码 |
| POST | `/captcha/send-sms-captcha` | `captchaType`, `username`, `uuid`, `verCode` | 向用户手机发验证码 |

### 用户 `/user`（需认证）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| GET | `/user/get-user-info` | - | 获取当前登录用户信息（从上下文取） |
| POST | `/user/update-user-info` | `mobile`, `email`, `nickname`, `idCard`, `age`, `genderType`, `birthday` | 更新当前用户 |

### 学生 `/student`（需认证）

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/student/get-by-id` | `id` | 按 ID 查询 |
| GET | `/student/get-by-mobile` | `mobile` | 按手机号查询 |
| GET | `/student/paged-list` | `page`, `size`, `userIdEq`, `gradeTypeEq`, `registerTimeStart`, `registerTimeEnd` | 分页查询（PageHelper） |
| POST | `/student/add-student` | `idCard`, `gradeType`, ... | 新增学生 |

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
