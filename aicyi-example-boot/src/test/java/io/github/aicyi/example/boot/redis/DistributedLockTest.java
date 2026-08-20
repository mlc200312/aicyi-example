package io.github.aicyi.example.boot.redis;

import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.commons.core.lock.DistributedLock;
import io.github.aicyi.commons.core.lock.DistributedLockManager;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.example.fixture.util.RedisLockStressFactory;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class DistributedLockTest extends BaseLoggerTest {

    @Autowired
    private DistributedLockManager distributedLockManager;

    private static final int LOCK = 1;

    @Before
    @Override
    public void beforeTest() {
    }

    @SneakyThrows
    @Test
    public void test() {
        RedisLockStressFactory factory = new RedisLockStressFactory(
                200,      // 200线程
                1000,     // 每线程1000次
                new RedisLockStressFactory.Robot() {
                    @Override
                    public DistributedLock getLock() {
                        return distributedLockManager.getLock("test-lock" + new Random().nextInt(LOCK));
                    }

                    @Override
                    public String working() {
                        try {
                            Thread.sleep(5); // 模拟业务
                        } catch (InterruptedException ignored) {
                        }
                        return "OK";
                    }
                });

        factory.startRun();
    }
}
