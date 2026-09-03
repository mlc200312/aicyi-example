package io.github.aicyi.example.web.dto;

import io.github.aicyi.commons.lang.DtoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:32
 **/
@Getter
@Setter
@Schema(description = "发送邮件验证码请求参数")
public class SendEmailCaptchaReq extends BaseBean implements DtoBean {
    @NotNull
    @Schema(description = "验证码类型")
    private Integer captchaType;
    @NotBlank
    @Schema(description = "用户名")
    private String username;
    @NotBlank
    @Schema(description = "uuid")
    private String uuid;
    @NotBlank
    @Schema(description = "验证码")
    private String verCode;
}
