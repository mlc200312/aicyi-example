package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:32
 **/
@Getter
@Setter
@ApiModel("发送SMS验证码请求参数")
public class SendSmsCaptchaReq extends BaseBean implements VoBean {
    @NotNull
    @ApiModelProperty("验证码类型")
    private Integer captchaType;
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("uuid")
    private String uuid;
    @NotBlank
    @ApiModelProperty("验证码")
    private String verCode;
}
