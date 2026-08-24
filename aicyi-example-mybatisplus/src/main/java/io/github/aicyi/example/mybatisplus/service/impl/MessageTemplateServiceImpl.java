package io.github.aicyi.example.mybatisplus.service.impl;

import io.github.aicyi.example.mybatisplus.domain.entity.MessageTemplate;
import io.github.aicyi.example.mybatisplus.mapper.MessageTemplateMapper;
import io.github.aicyi.example.mybatisplus.service.IMessageTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息模版表 服务实现类
 * </p>
 *
 * @author Leno
 * @since 2026-08-24
 */
@Service
public class MessageTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageTemplateService {

}
