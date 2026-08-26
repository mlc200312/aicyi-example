# 快速开始

本文档引导你**从零到跑通主应用**（`aicyi-example-boot`），约 10 分钟。其他三个示例应用的启动方式见文末及对应文档。

## 环境要求

| 软件 | 版本 | 用途 |
| --- | --- | --- |
| JDK | 1.8+ | 运行环境 |
| Maven | 3.6+ | 构建 |
| MySQL | 8.0+ | 业务库（t_user / t_student / message_template） |
| Redis | 5.0+ | 缓存、分布式锁、Snowflake WorkerId、Token 存储 |
| Nacos Server | 2.x | 配置中心（所有环境配置存放于此） |
| RabbitMQ | 3.8+ | 仅 `aicyi-example-rabbitmq` 与主应用的 MQ 能力需要（可选） |
| Docker | 任意 | 快速启动 Nacos / RabbitMQ（推荐） |

> 主应用 `application-{profile}.yml` 中声明了 Nacos 导入清单（含 `aicyi-rabbitmq.yml`），
> 因此**主应用启动时也会连接 RabbitMQ**；若本地未准备 RabbitMQ，可把导入清单中的
> `aicyi-rabbitmq.yml` 移除后重启（见 [常见问题](#常见问题)）。

## 1. 基础设施准备

### 1.1 MySQL

确保本机 MySQL 8 可连接（默认 `localhost:3306`，root/root）。连接参数可在
`nacos/aicyi-datasource.yml` 中调整。

### 1.2 Redis

确保 Redis 可连接（默认 `localhost:6379`，密码 root）。连接参数见 `nacos/aicyi-redis.yml`。

### 1.3 Nacos

启动 Nacos Server（默认 `127.0.0.1:8848`，可用环境变量 `NACOS_SERVER_ADDR` 覆盖）：

```bash
docker run -d --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.2.3
```

创建 test 环境命名空间（本地已内置默认值 `29b4684f-0751-4290-a0a4-f65a51893ef6`），
并把 `nacos/` 目录下的配置逐个导入该命名空间。详细步骤见 [Nacos 配置中心](./infra/nacos.md)。

### 1.4 RabbitMQ（可选）

仅当需要跑主应用的 MQ 能力或 `aicyi-example-rabbitmq` 应用时准备。
Docker 容器、应用账号、延迟插件与交换机/队列拓扑的完整初始化见 [RabbitMQ 本地环境](./infra/rabbitmq.md)。

## 2. 初始化数据库

执行 `aicyi-example-dao/db/init.sql` 创建示例表：

```bash
mysql -uroot -proot -h127.0.0.1 < aicyi-example-dao/db/init.sql
```

脚本会创建：
- `t_user` — 用户表
- `t_student` — 学生表
- `message_template` — 消息模板表

## 3. 构建项目

```bash
# 1) 先构建并安装 aicyi 基础包（BOM/commons/midware）
cd <aicyi-仓库路径>
mvn clean install -DskipTests

# 2) 再构建本工程
cd <aicyi-example-仓库路径>
mvn clean install -DskipTests
```

## 4. 启动主应用

```bash
cd aicyi-example-boot
mvn spring-boot:run
```

启动成功后监听 **80 端口**，日志出现 `Started AicyiExampleApplication` 即成功。

## 5. 验证接口

### 5.1 获取图形验证码（登录需要）

```bash
curl http://localhost/captcha/get-captcha
```

返回 `data.uuid` 与 `data.captcha`（图片地址）。浏览器打开图片地址查看验证码，登录时提交 `uuid` + `verCode`。
（test 环境默认跳过验证码一致性校验，`verCode` 传任意值即可。）

### 5.2 用户注册

```bash
curl -X POST http://localhost/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "123456",
    "mobile": "13800138000"
  }'
```

### 5.3 用户登录

```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "123456",
    "uuid": "<5.1 的 uuid>",
    "verCode": "<图片验证码>"
  }'
```

返回 `data.token.accessToken` / `data.token.refreshToken`。

### 5.4 访问需认证接口

```bash
curl http://localhost/user/get-user-info \
  -H "Authorization: Bearer <5.3 的 accessToken>"
```

### 5.5 刷新 Token

```bash
curl -X POST http://localhost/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "<5.3 的 refreshToken>" }'
```

### 5.6 查看接口文档

浏览器访问 `http://localhost/api-doc.html`（springfox OpenAPI 3）。

> 全部接口清单与请求/响应示例见 [apps/aicyi-example-boot.md](./apps/aicyi-example-boot.md)。

## 6. 其他应用快速启动

| 应用 | 端口 | 前置 | 启动命令 | 文档 |
| --- | --- | --- | --- | --- |
| `aicyi-example-mybatisplus` | 8081 | MySQL + Nacos | `cd aicyi-example-mybatisplus && mvn spring-boot:run` | [apps/mybatisplus.md](./apps/aicyi-example-mybatisplus.md) |
| `aicyi-example-rabbitmq` | 8082 | RabbitMQ + Nacos | `cd aicyi-example-rabbitmq && mvn spring-boot:run` | [apps/rabbitmq.md](./apps/aicyi-example-rabbitmq.md) |
| `aicyi-example-xxljob` | 8083 | XXL-Job 调度中心 + Nacos | `cd aicyi-example-xxljob && mvn spring-boot:run` | [apps/xxljob.md](./apps/aicyi-example-xxljob.md) |

> 所有应用均通过 Nacos 拉取配置，启动前请先完成 [Nacos 配置导入](./infra/nacos.md)。

## 常见问题

### Q: 启动报 Nacos 配置导入失败？

Nacos 未启动、namespace 不对，或 `nacos/` 下的 Data ID 未完整导入目标命名空间。核对
`NACOS_SERVER_ADDR` / `NACOS_NAMESPACE` 环境变量；导入为非 optional，缺失即启动失败。

### Q: 启动报 Redis / MySQL 连接失败？

检查 `nacos/aicyi-redis.yml` / `nacos/aicyi-datasource.yml` 中的 host/port/账号密码是否与本机一致。

### Q: 没有 RabbitMQ 也能启动主应用吗？

可以。编辑 `aicyi-example-boot/src/main/resources/application-test.yml`，从 `spring.config.import`
中移除 `nacos:aicyi-rabbitmq.yml?...` 这一行（并同步移除 `application-prod.yml` 中对应行）即可；
主应用其余能力不受影响。

### Q: 如何跑单元/集成测试？

boot 模块内测试直接依赖本机 Redis/MySQL（见 `aicyi-example-fixture` 的测试夹具）：
`mvn test -pl aicyi-example-boot`。纯单测在 `aicyi-example-test` 模块：`mvn test -pl aicyi-example-test`。

### Q: 主应用启动报 `documentationPluginsBootstrapper` NPE？

这是 springfox 3.0.0 与 Spring Boot 2.6+ 路径匹配策略的兼容问题，需要在配置中保留
`spring.mvc.path-match.matching-strategy: ant_path_matcher`（已放在共享配置 `nacos/aicyi-example.yml`
与各应用本地 `application.yml` 中，勿删除）。
