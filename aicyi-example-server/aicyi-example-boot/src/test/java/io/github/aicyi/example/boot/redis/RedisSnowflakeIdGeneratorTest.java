package io.github.aicyi.example.boot.redis;

import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.util.BaseLoggerTest;
import io.github.aicyi.commons.core.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:45
 **/
@SpringBootTest(classes = AicyiExampleApplication.class)
public class RedisSnowflakeIdGeneratorTest extends BaseLoggerTest {

    @Autowired
    private IdGenerator idGenerator;

    @BeforeEach
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
