package io.github.aicyi.example.boot.redis;

import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.commons.core.id.IdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:45
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class RedisSnowflakeIdGeneratorTest extends BaseLoggerTest {

    @Autowired
    private IdGenerator idGenerator;

    @Before
    @Override
    public void beforeTest() {
    }

    @Override
    @Test
    public void test() {
        for (int i = 0; i < 50; i++) {
            long id = idGenerator.nextId();
            System.out.println(id);
        }
    }
}
