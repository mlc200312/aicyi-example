# Nacos 配置中心

所有可运行应用均通过 `spring.config.import` 从 Nacos 读取配置（spring-cloud-alibaba 2023.0.1.2，无需 `bootstrap.yml`）。
本仓库 `nacos/` 目录下的 YAML 即各 Data ID 的源文件，需手动导入 Nacos 对应命名空间。

## 环境隔离

不同环境使用不同 namespace，声明在各自的 `application-{profile}.yml` 中（导入语句也在同文件，确保同一 namespace 下解析）：

| profile | namespace 来源 | 说明 |
| --- | --- | --- |
| test | `${NACOS_NAMESPACE:29b4684f-0751-4290-a0a4-f65a51893ef6}` | 内置默认值，可用环境变量覆盖 |
| prod | `${NACOS_NAMESPACE}` | 必须由环境变量注入，未设置时启动报错，防止误连其他环境 |

新增环境：复制 `application-prod.yml` 为 `application-{新环境}.yml`，填入对应 namespace 与导入清单即可。

## Data ID 清单

`nacos/` 目录下所有配置均需导入 **test 环境命名空间**（prod 环境按需导入 `aicyi-example-prod.yml`）：

| Data ID | 说明 | 引用它的应用 |
| --- | --- | --- |
| `aicyi-example.yml` | 共享：MyBatis 日志（`IbatisLogger`）、freemarker | boot |
| `aicyi-example-test.yml` | test 环境：snowflake、message（邮箱/短信/MQ） | boot |
| `aicyi-example-prod.yml` | prod 环境：snowflake、message（凭证为环境变量占位） | boot（prod） |
| `aicyi-example-rabbitmq.yml` | 共享：消费函数声明、Stream 绑定与交换机/路由键 | rabbitmq |
| `aicyi-datasource.yml` | 数据源（MySQL `test` 库） | boot / mybatisplus |
| `aicyi-redis.yml` | Redis 连接 | boot |
| `aicyi-rabbitmq.yml` | RabbitMQ 连接与 binder | boot / rabbitmq |
| `aicyi-xxl-job.yml` | XXL-Job 调度中心与执行器参数 | xxljob |

> 各应用实际导入哪些 Data ID，以对应模块 `src/main/resources/application-{profile}.yml` 中的
> `spring.config.import` 为准。

## 导入步骤

1. 启动 Nacos Server（默认 `127.0.0.1:8848`，可用环境变量 `NACOS_SERVER_ADDR` 覆盖）：

   ```bash
   docker run -d --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.2.3
   ```

2. Nacos 控制台 → 命名空间 → 新建命名空间，为每个环境创建独立 namespace，记录命名空间 ID。
3. 切换到目标命名空间 → 配置管理 → 配置列表 → 新建配置。
4. 按上表逐个创建：**Data ID、Group（DEFAULT_GROUP）保持一致，配置格式选 YAML，内容粘贴自 `nacos/` 对应文件**。

## 注意事项

- 导入为非 optional，Nacos 不可达或配置缺失时应用将启动失败；如需本地容错可改为 `optional:nacos:...`。
- Nacos 开启鉴权时，通过环境变量 `NACOS_USERNAME` / `NACOS_PASSWORD` 注入账号密码。
- 配置项开启 `refreshEnabled=true`，配合 `@RefreshScope` 可实现动态刷新。
- 敏感凭证（邮箱/短信/XXL-Job Token）保持 `${...}` 环境变量占位符形式，实际值由部署环境注入，不写入配置中心明文。
- `aicyi-example.yml` 里**不需要**也**不应该**再放 `spring.mvc.path-match.matching-strategy: ant_path_matcher`：
  那是 springfox 3.0 的兼容必需项（springfox 依赖 `AntPathMatcher` 解析其内部路径模式，遇上 Boot 3 默认的
  `PathPatternParser` 会抛 NPE）；springdoc-openapi 2.x 不依赖它。保留会让整个 MVC 从 `PathPatternParser`
  退回 `AntPathMatcher`，属于没有收益的回退。当前 `aicyi-example.yml` 已移除该项，只保留 MyBatis 日志与 freemarker 两项。

## 相关文档

- [RabbitMQ 本地环境](./rabbitmq.md)
- [应用启动](../quickstart.md)
