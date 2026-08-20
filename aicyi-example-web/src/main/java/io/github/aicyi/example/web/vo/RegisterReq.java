package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * @author Mr.Min
 * @description 注册请求参数
 * @date 15:05
 **/
@Getter
@Setter
@ApiModel("注册请求参数")
public class RegisterReq extends BaseBean implements VoBean {
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("密码")
    private String password;
    @NotBlank
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("生日")
    private LocalDate birthday;
    @ApiModelProperty("性别")
    private Integer genderType;
}
