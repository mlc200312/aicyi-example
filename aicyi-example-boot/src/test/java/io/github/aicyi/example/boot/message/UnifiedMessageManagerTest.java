package io.github.aicyi.example.boot.message;

import io.github.aicyi.commons.util.DateTimeUtils;
import io.github.aicyi.commons.util.JsonUtils;
import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.commons.util.UUIDUtils;
import io.github.aicyi.midware.utils.IdUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.service.channel.MessageChannels;
import io.github.aicyi.midware.message.mail.model.MailMessage;
import io.github.aicyi.midware.message.mq.model.MqMessage;
import io.github.aicyi.commons.core.message.MessageSendResult;
import io.github.aicyi.commons.core.message.MessageSendCallback;
import io.github.aicyi.midware.message.core.sender.UnifiedMessageManager;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.midware.message.sms.model.SmsMessage;
import io.github.aicyi.example.fixture.util.DataSource;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 16:12
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class UnifiedMessageManagerTest extends BaseLoggerTest {

    @Autowired
    private UnifiedMessageManager unifiedMessageManager;

    @Before
    @Override
    public void beforeTest() {
    }

    @Test
    @Override
    public void test() {
        Map<String, Object> map = Maps.ofStr("code", "123456").and("expireMinutes", "1000").build();
        SmsMessage smsMessage = SmsMessage.of("15910436675", "SMS_REGISTER_CODE", map);
        smsMessage.setBusinessId(UUIDUtils.generateV7Id());

        MessageSendResult result = unifiedMessageManager.send(smsMessage);

        log(result);
    }

    @SneakyThrows
    @Test
    public void test2() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Map<String, Object> map = Maps.ofStr("orderNo", "123456").and("amount", "1000").build();

        SmsMessage smsMessage = SmsMessage.of("15910436675", "SMS_PAYMENT_SUCCESS", map);
        unifiedMessageManager.sendAsync(smsMessage, new MessageSendCallback() {
            @Override
            public void onComplete(MessageSendResult result) {
                log("短信发送完成：" + result);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                log("短信发送失败：" + e.getMessage());
            }
        });

        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void test3() {
        MqMessage mqMessage = MqMessage.of(DataSource.getUser(), MessageChannels.OUTPUT);
        MessageSendResult result = unifiedMessageManager.send(mqMessage);

        log(result);
    }

    @SneakyThrows
    @Test
    public void test4() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        MqMessage mqMessage = MqMessage.of(DataSource.getUser(), MessageChannels.OUTPUT);
        unifiedMessageManager.sendAsync(mqMessage, new MessageSendCallback() {
            @Override
            public void onComplete(MessageSendResult result) {
                log("消息发送完成：" + result);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                log("消息发送失败：" + e.getMessage());
            }
        });

        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void test5() {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("orderId", IdUtils.generateId() + "");
        templateParams.put("userName", "王五");
        templateParams.put("orderDate", DateTimeUtils.format(LocalDateTime.now(), DateTimeUtils.DATE_PATTERN));
        Map<String, Object> build1 = Maps.ofStr("name", "笔记本电脑")
                .and("price", 5999.00)
                .and("quantity", 1)
                .build();
        Map<String, Object> build2 = Maps.ofStr("name", "鼠标")
                .and("price", 199.00)
                .and("quantity", 2)
                .build();
        List<Map<String, Object>> products = Arrays.asList(build1, build2);
        templateParams.put("products", products);
        templateParams.put("totalAmount", 6397.00);

        MailMessage mailMessage = MailMessage.of("15910436675@163.com", "EMAIL_USER_ORDER_INFO", templateParams);
        MessageSendResult result = unifiedMessageManager.send(mailMessage);

        log(result);
    }

    @SneakyThrows
    @Test
    public void test6() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String json = "{\n" +
                "    \"emailData\": {\n" +
                "        \"title\": \"FlashPay payment notice\",\n" +
                "        \"merchName\": \"FlashPay\",\n" +
                "        \"cur\": \"THB\",\n" +
                "        \"amount\": \"1000\",\n" +
                "        \"paymentLink\": \"www.flashfin.com\",\n" +
                "        \"outTradeNo\": \"outTradeNo\",\n" +
                "        \"tradeNo\": \"tradeNo\",\n" +
                "        \"subject\": \"subject\",\n" +
                "        \"tradeTime\": \"2019-09-09 09:09:09\",\n" +
                "        \"expireTime\": \"2024-12-01 23:59:59\"\n" +
                "    }\n" +
                "}";

        Map<String, Object> templateParams = JsonUtils.getInstance().fromJsonMap(json, String.class, Object.class);

        MailMessage mailMessage = MailMessage.of("15910436675@163.com", "EMAIL_PAYMENT_NOTICE", templateParams);

        unifiedMessageManager.sendAsync(mailMessage, new MessageSendCallback() {
            @Override
            public void onComplete(MessageSendResult result) {
                log("消息发送完成：" + result);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                log("消息发送失败：" + e.getMessage());
            }
        });

        countDownLatch.await(10, TimeUnit.SECONDS);
    }
}
