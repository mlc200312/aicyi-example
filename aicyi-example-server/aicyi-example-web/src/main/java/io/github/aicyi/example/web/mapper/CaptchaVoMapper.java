package io.github.aicyi.example.web.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.bo.SendCaptchaParam;
import io.github.aicyi.example.web.dto.SendEmailCaptchaReq;
import io.github.aicyi.example.web.dto.SendSmsCaptchaReq;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 验证码模块 VO 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = EnumTypeConverters.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CaptchaVoMapper {

    /**
     * 邮件验证码请求 -> 验证码参数（captchaType 由 Integer code 转枚举）
     */
    SendCaptchaParam toSendCaptchaParam(SendEmailCaptchaReq req);

    /**
     * 短信验证码请求 -> 验证码参数（captchaType 由 Integer code 转枚举）
     */
    SendCaptchaParam toSendCaptchaParam(SendSmsCaptchaReq req);
}
