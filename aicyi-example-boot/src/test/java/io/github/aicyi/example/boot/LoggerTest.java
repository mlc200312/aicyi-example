package io.github.aicyi.example.boot;

import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.commons.logging.LoggerType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 20:36
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class LoggerTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() {
        logger.error(new RuntimeException("不支持的操作类型"), "测试失败日志打印");
    }
}
