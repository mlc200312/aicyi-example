package io.github.aicyi.example.boot.message;

import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.midware.message.sms.model.SmsMessage;
import io.github.aicyi.midware.message.sms.sender.SmsSender;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 18:41
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class SmsSenderTest extends BaseLoggerTest {

    @Autowired
    private SmsSender smsSender;

    private List<String> numbers;
    private String content;
    private String sign;

    @Before
    @Override
    public void beforeTest() {
        numbers = Arrays.asList("15910436675", "13661371201");
        content = "测试短信";
        sign = "测试";
    }

    @Test
    @Override
    public void test() {

        boolean isTrue = smsSender.send(numbers.get(0), content, sign);

        log(isTrue);
    }

    @SneakyThrows
    @Test
    public void test2() {

        CompletableFuture<Boolean> async = smsSender.sendAsync(numbers, content, sign);

        Boolean isTrue = async.get();

        log(isTrue);
    }

    @Test
    public void test3() {

        Map<String, Object> templateParams = Maps.ofStr("code", "123").and("expireMinutes", "1000").build();

        SmsMessage smsMessage = SmsMessage.of(numbers, "SMS_LOGIN_CODE", templateParams);

        boolean isTrue = smsSender.sendTemplate(smsMessage);

        log(isTrue);
    }
}
