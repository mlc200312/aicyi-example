# Nacos 配置中心导入说明

应用通过 `spring.config.import` 从 Nacos 配置中心读取配置（spring-cloud-alibaba 2021.0.5.0，无需 bootstrap.yml）。

## 需要导入的配置

| Data ID | Group | 格式 | 说明 |
| --- | --- | --- | --- |
| `aicyi-example.yml` | DEFAULT_GROUP | YAML | 共享配置（所有环境） |
| `aicyi-example-test.yml` | DEFAULT_GROUP | YAML | test 环境配置 |

## 导入步骤

1. 启动 Nacos Server（默认 `127.0.0.1:8848`，可用环境变量 `NACOS_SERVER_ADDR` 覆盖）。
2. 打开 Nacos 控制台 → 配置管理 → 配置列表 → 新建配置。
3. 按上表逐个创建：Data ID、Group 保持一致，配置格式选 YAML，内容粘贴自本目录对应文件。

## 注意事项

- 导入为非 optional，Nacos 不可达或配置缺失时应用将启动失败；如需本地容错可改为 `optional:nacos:...`。
- Nacos 开启鉴权时，通过环境变量 `NACOS_USERNAME` / `NACOS_PASSWORD` 注入账号密码。
- 配置项开启 `refreshEnabled=true`，配合 `@RefreshScope` 可实现动态刷新。
- 敏感凭证（邮箱/短信）保持环境变量占位符形式，实际值由部署环境注入。
