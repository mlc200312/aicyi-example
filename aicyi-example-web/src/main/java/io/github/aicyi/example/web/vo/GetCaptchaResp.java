package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 学生类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@Schema(description = "获取验证码响应参数")
public class GetCaptchaResp extends BaseBean implements VoBean {
    @Schema(description = "验证码")
    private String captcha;
    @Schema(description = "uuid")
    private String uuid;
}
