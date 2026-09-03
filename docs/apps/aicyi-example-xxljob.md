# aicyi-example-xxljob（XXL-Job 定时任务）

演示 aicyi 工程集成 XXL-Job 2.5.0 作为**任务执行器**（Executor）。

- 端口：**8083**
- 启动类：`io.github.aicyi.example.xxljob.AicyiExampleXxlJobApplication`

## 前置：XXL-Job 调度中心

需先运行 XXL-Job Admin（调度中心），默认地址 `http://127.0.0.1:8080/xxl-job-admin`
（可在 `nacos/aicyi-xxl-job.yml` 中调整 `xxl.job.admin.addresses`）。

```bash
docker run -d --name xxl-job -p 8080:8080 \
  -e PARAMS="--spring.datasource.url=jdbc:mysql://<host>:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8 \
             --spring.datasource.username=root --spring.datasource.password=root" \
  xuxueli/xxl-job-admin:2.5.0
```

> 调度中心镜像版本需与执行器依赖的 `xxl-job-core`（**2.5.0**，由 BOM 统一管理）保持一致，跳版可能出现注册/回调协议不兼容。
> 只需映射 `8080`（调度中心 Web 端口）；执行器端口 `9998` 由**应用进程**在宿主机监听，不属于容器，无需映射。
> 若应用与调度中心同处一个 docker 网络，`admin.addresses` 改用容器名（如 `http://xxl-job:8080/xxl-job-admin`）。

> 调度中心需要自己的 MySQL 库（`xxl_job`），可用官方 `tables_xxl_job.sql` 初始化。

## 启动

```bash
cd aicyi-example-xxljob
mvn spring-boot:run
```

启动后执行器注册到调度中心（appname `aicyi-executor`，端口 9998）。

## 配置

### 本地 `application.yml`

- `server.port: 8083`，`spring.profiles.active: test`
- 依赖 `spring-boot-starter-web`：无 Web 时应用为非 Web 进程会随上下文启动后退出，执行器无法存活

### Nacos 导入清单（`application-test.yml`）

| Data ID | 作用 |
| --- | --- |
| `aicyi-xxl-job.yml` | 调度中心地址、执行器参数、AccessToken |

`xxl.job.*` 关键项：

| 配置 | 值 | 说明 |
| --- | --- | --- |
| `xxl.job.admin.addresses` | `http://127.0.0.1:8080/xxl-job-admin` | 调度中心地址 |
| `xxl.job.executor.appname` | `aicyi-executor` | 执行器名称（调度中心注册时需一致） |
| `xxl.job.executor.port` | `9998` | 执行器回调端口 |
| `xxl.job.accessToken` | `Aicyi_XXLJOB@2026_Secret888` | 与调度中心配置的令牌一致 |
| `xxl.job.executor.logpath` | `./logs/xxl-job/jobhandler` | 任务日志目录 |
| `xxl.job.executor.logretentiondays` | `30` | 任务日志保留天数 |

## 任务示例（`handler` 包）

| JobHandler | 说明 |
| --- | --- |
| `demoSimpleJob` | 简单任务：读取任务参数、模拟执行、`handleSuccess/handleFail` |
| `demoShardingJob` | 分片广播任务：`getShardIndex` / `getShardTotal` |

## 在调度中心配置任务

1. 登录 XXL-Job Admin（默认 admin/123456）。
2. 执行器管理 → 新增执行器：AppName 填 `aicyi-executor`，注册方式选「自动注册」。
3. 任务管理 → 新增任务：JobHandler 填 `demoSimpleJob`（或 `demoShardingJob`），配置 Cron 与参数。
4. 启动任务后，观察应用日志输出与任务执行结果。
