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
@ApiModel("更新用户密码请求参数")
public class UpdatePasswordReq extends BaseBean implements VoBean {
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("新密码")
    private String newPassword;
    @NotBlank
    @ApiModelProperty("uuid")
    private String uuid;
    @NotBlank
    @ApiModelProperty("验证码")
    private String verCode;
}
