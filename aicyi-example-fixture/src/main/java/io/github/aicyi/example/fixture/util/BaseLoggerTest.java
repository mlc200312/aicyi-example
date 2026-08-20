package io.github.aicyi.example.fixture.util;


import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;

/**
 * @author Mr.Min
 * @description 基础日志测试
 * @date 2020-04-20
 **/
public abstract class BaseLoggerTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public void log(Object... os) {
        int i = 0;
        StringBuilder sb = new StringBuilder(String.format("\n %s execute %s，输出的结果 : \n", this.getClass().getName(), "test"));
        for (Object o : os) {
            sb.append(++i).append("、").append(o).append("\n");
        }
        logger.info(sb);
    }

    public abstract void beforeTest();

    public abstract void test();
}
