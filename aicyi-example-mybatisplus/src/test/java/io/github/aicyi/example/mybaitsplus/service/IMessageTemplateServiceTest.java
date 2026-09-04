package io.github.aicyi.example.mybaitsplus.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.aicyi.example.mybatisplus.AicyiExampleMyBatisPlusApplication;
import io.github.aicyi.example.mybatisplus.domain.entity.MessageTemplate;
import io.github.aicyi.example.mybatisplus.service.IMessageTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 2026/8/21
 **/
@SpringBootTest(classes = AicyiExampleMyBatisPlusApplication.class)
public class IMessageTemplateServiceTest {

    @Autowired
    private IMessageTemplateService messageTemplateService;

    @Test
    public void save() {

        QueryWrapper<MessageTemplate> wrapper = new QueryWrapper<MessageTemplate>()
                .eq("template_code", "Test");
        messageTemplateService.remove(wrapper);

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setTemplateCode("Test_01");
        messageTemplate.setTemplateName("测试");
        messageTemplate.setMessageType("SMS");
        messageTemplate.setFormat("TEXT");
        messageTemplate.setEngineType("ENGINE_TYPE");
        messageTemplate.setSubject("Subject");
        messageTemplate.setContent("Content");
        messageTemplate.setSignature("Signature");
        messageTemplate.setVariables(new ArrayList<>(List.of("variable1", "variable2", "variable3")));
        messageTemplate.setEnabled(true);
        messageTemplate.setRemark("Remark");
        messageTemplate.setDeleted(false);
        messageTemplate.setVersion(1);
        messageTemplate.setCreateTime(LocalDateTime.now());
        messageTemplate.setUpdateTime(LocalDateTime.now());

        messageTemplateService.save(messageTemplate);
    }

    @Test
    public void update() {
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setId(6L);
        messageTemplate.setVariables(new ArrayList<>(List.of("variable1", "variable2", "variable3", "variable4", "variable5")));

        messageTemplateService.updateById(messageTemplate);
    }

    @Test
    public void get() {
        List<MessageTemplate> list = messageTemplateService.list();

        System.out.println(list);
    }
}
