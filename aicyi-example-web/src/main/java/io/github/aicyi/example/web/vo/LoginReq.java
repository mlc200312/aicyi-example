package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Mr.Min
 * @description 登录请求参数
 * @date 15:05
 **/
@Getter
@Setter
@ApiModel("登录请求参数")
public class LoginReq extends BaseBean implements VoBean {
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("密码")
    private String password;
    @NotBlank
    @ApiModelProperty("uuid")
    private String uuid;
    @NotBlank
    @ApiModelProperty("验证码")
    private String verCode;
}
