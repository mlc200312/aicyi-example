package io.github.aicyi.example.xxljob.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DemoXxlJobHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoXxlJobHandler.class);


    /**
     * 简单定时任务示例
     * jobHandler名称：demoSimpleJob，后台新增任务时填写这个名字
     */
    @XxlJob("demoSimpleJob")
    public void demoSimpleJob() {
        log.info("==== demoSimpleJob 任务开始 ====");
        // 获取任务参数
        String jobParam = XxlJobHelper.getJobParam();
        log.info("任务入参：{}", jobParam);

        // 业务逻辑
        try {
            TimeUnit.SECONDS.sleep(2);
            // 设置执行成功
            XxlJobHelper.handleSuccess("执行完成，自定义返回信息");
        } catch (Exception e) {
            log.error("任务执行异常", e);
            // 设置执行失败，会触发告警
            XxlJobHelper.handleFail("任务异常：" + e.getMessage());
        }
        log.info("==== demoSimpleJob 任务结束 ====");
    }


    /**
     * 分片广播任务示例
     */
    @XxlJob("demoShardingJob")
    public void demoShardingJob() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("分片任务：shardIndex={},shardTotal={}", shardIndex, shardTotal);

        XxlJobHelper.handleSuccess();
    }
}
