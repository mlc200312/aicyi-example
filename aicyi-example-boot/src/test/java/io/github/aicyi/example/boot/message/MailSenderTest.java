package io.github.aicyi.example.boot.message;

import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.commons.util.DateTimeUtils;
import io.github.aicyi.midware.utils.IdUtils;
import io.github.aicyi.midware.message.mail.model.MailMessage;
import io.github.aicyi.midware.message.mail.sender.EmailSender;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.midware.message.mail.model.MailAttachment;
import io.github.aicyi.commons.core.template.TemplateEngine;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 18:41
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class MailSenderTest extends BaseLoggerTest {

    @Autowired
    private EmailSender emailSender;

    private List<String> toList;
    private MailAttachment attachment;
    private TemplateEngine templateEngine;

    @Before
    @Override
    public void beforeTest() {
        toList = Arrays.asList("15910436675@163.com", "mlc200312@163.com");

        attachment = new MailAttachment();
        attachment.setName("test.xlsx");
        String absolutePath = new File("").getAbsoluteFile().getParentFile().getPath();
        File file = new File(absolutePath + "/aicyi-example-test/src/test/resources/test/bank_insert.xlsx");
        attachment.setFile(file);
        attachment.setContentType("xlsx");

        // 创建模拟的TemplateEngine
        templateEngine = Mockito.mock(TemplateEngine.class);

        boolean connection = emailSender.testConnection();

        assert connection;
    }

    @SneakyThrows
    @Test
    @Override
    public void test() {

        String content = "TEST TEXT";

        boolean isSend = emailSender.sendText(toList, "这是一个TEXT", content);

        assert isSend;
    }


    @SneakyThrows
    @Test
    public void test1() {

        // mock TemplateEngine
        TemplateEngine templateEngine = Mockito.mock(TemplateEngine.class);

        // mock 模板名
        String templateName = "email-template";

        // mock 模板参数
        Map<String, Object> templateParams = new HashMap<>();

        templateParams.put("employeeName", "张三");
        templateParams.put("phoneNumber", "13800000000");
        templateParams.put("email", "zhangsan@example.com");
        templateParams.put("jobTitle", "Java开发工程师");
        templateParams.put("departmentName", "技术研发中心");
        templateParams.put("onboardingDate", "2026-05-09");
        templateParams.put("workLocation", "新加坡 Tanjong Pagar");

        // mock 返回HTML
        String expectedHtml =
                "<!DOCTYPE html>\n" +
                        "                        <html>\n" +
                        "                        <body>\n" +
                        "                            <h1>入职申请</h1>\n" +
                        "                            <p>姓名：张三</p>\n" +
                        "                        </body>\n" +
                        "                        </html>";

        // mock process 方法
        Mockito.when(
                templateEngine.process(
                        Mockito.eq(templateName),
                        Mockito.any()
                )
        ).thenReturn(expectedHtml);

        // 调用 mock 方法
        String actualHtml = templateEngine.process(templateName, templateParams);

        // 断言
        Assertions.assertEquals(expectedHtml, actualHtml);

        // 验证调用次数
        Mockito.verify(templateEngine, Mockito.times(1))
                .process(
                        Mockito.eq(templateName),
                        Mockito.any()
                );

        boolean isSend = emailSender.sendHtml(toList, "这是一个Html", actualHtml);

        assert isSend;
    }

    @SneakyThrows
    @Test
    public void test2() {

        List<MailAttachment> attachmentList = Arrays.asList(attachment);

        boolean isSend = emailSender.sendWithAttachment(toList, "附件", "测试带附件的邮件", attachmentList);

        assert isSend;
    }

    @SneakyThrows
    @Test
    public void test3() {
        CompletableFuture<Boolean> async = emailSender.sendAsync(toList, "异步邮件", "测试异步发送邮件");

        assert async.get();

        log(async.get());
    }

    @SneakyThrows
    @Test
    public void test4() {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("orderId", IdUtils.generateId() + "");
        templateParams.put("userName", "王五");
        templateParams.put("orderDate", DateTimeUtils.format(LocalDateTime.now(), DateTimeUtils.DATE_PATTERN));
        Map<String, Object> build1 = Maps.of("", new Object())
                .and("name", "笔记本电脑")
                .and("price", 5999.00)
                .and("quantity", 1)
                .build();
        Map<String, Object> build2 = Maps.of("", new Object())
                .and("name", "鼠标")
                .and("price", 199.00)
                .and("quantity", 2)
                .build();
        List<Map<String, Object>> products = Arrays.asList(build1, build2);
        templateParams.put("products", products);
        templateParams.put("totalAmount", 6397.00);

        MailMessage mailMessage = MailMessage.of(toList, "EMAIL_USER_ORDER_INFO", templateParams);

        // 执行测试
        boolean isSend = emailSender.sendTemplate(mailMessage);

        assert isSend;
    }
}
