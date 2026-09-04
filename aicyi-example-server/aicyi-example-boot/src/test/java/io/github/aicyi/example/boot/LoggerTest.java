package io.github.aicyi.example.boot;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 20:36
 **/
@SpringBootTest(classes = AicyiExampleApplication.class)
public class LoggerTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() {
        logger.error(new RuntimeException("不支持的操作类型"), "测试失败日志打印");
    }
}
