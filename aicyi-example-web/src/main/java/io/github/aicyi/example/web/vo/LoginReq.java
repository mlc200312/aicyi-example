package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Mr.Min
 * @description 登录请求参数
 * @date 15:05
 **/
@Getter
@Setter
@Schema(description = "登录请求参数")
public class LoginReq extends BaseBean implements VoBean {
    @NotBlank
    @Schema(description = "用户名")
    private String username;
    @NotBlank
    @Schema(description = "密码")
    private String password;
    @NotBlank
    @Schema(description = "uuid")
    private String uuid;
    @NotBlank
    @Schema(description = "验证码")
    private String verCode;
}
