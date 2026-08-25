# Nacos 配置中心导入说明

应用通过 `spring.config.import` 从 Nacos 配置中心读取配置（spring-cloud-alibaba 2021.0.5.0，无需 bootstrap.yml）。

## namespace 按环境隔离

不同环境使用不同 namespace，声明在各自的 `application-{profile}.yml` 中（导入语句也在同文件，确保同一 namespace 下解析）：

| profile | namespace 来源 | 说明 |
| --- | --- | --- |
| test | `${NACOS_NAMESPACE:29b4684f-0751-4290-a0a4-f65a51893ef6}` | 内置默认值，可用环境变量覆盖 |
| prod | `${NACOS_NAMESPACE}` | 必须由环境变量注入，未设置时启动报错，防止误连其他环境 |

新增环境：复制 `application-prod.yml` 为 `application-{新环境}.yml`，填入对应 namespace 与导入清单即可。

## 需要导入的配置

| Data ID | Group | 格式 | 说明 |
| --- | --- | --- | --- |
| `aicyi-example.yml` | DEFAULT_GROUP | YAML | 共享配置（所有环境，需在每个 namespace 各导入一份） |
| `aicyi-example-test.yml` | DEFAULT_GROUP | YAML | test 环境配置 |
| `aicyi-example-prod.yml` | DEFAULT_GROUP | YAML | prod 环境配置 |
| `aicyi-redis.yml` | DEFAULT_GROUP | YAML | redis 环境配置 |
| `aicyi-rabbitmq.yml` | DEFAULT_GROUP | YAML | RabbitMQ 环境配置 |

## 导入步骤

1. 启动 Nacos Server（默认 `127.0.0.1:8848`，可用环境变量 `NACOS_SERVER_ADDR` 覆盖）。
2. Nacos 控制台 → 命名空间 → 新建命名空间，为每个环境创建独立 namespace，记录命名空间 ID。
3. 切换到目标命名空间 → 配置管理 → 配置列表 → 新建配置。
4. 按上表逐个创建：Data ID、Group 保持一致，配置格式选 YAML，内容粘贴自本目录对应文件。

## 注意事项

- 导入为非 optional，Nacos 不可达或配置缺失时应用将启动失败；如需本地容错可改为 `optional:nacos:...`。
- Nacos 开启鉴权时，通过环境变量 `NACOS_USERNAME` / `NACOS_PASSWORD` 注入账号密码。
- 配置项开启 `refreshEnabled=true`，配合 `@RefreshScope` 可实现动态刷新。
- 敏感凭证（邮箱/短信）保持环境变量占位符形式，实际值由部署环境注入。
