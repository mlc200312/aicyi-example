package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.example.domain.type.GenderType;
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
@ApiModel("更新用户信息请求参数")
public class UpdateUserInfoReq extends BaseBean implements VoBean {
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("邮箱地址")
    private String email;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("身份证")
    private String idCard;
    @ApiModelProperty("年龄")
    private Integer age;
    @ApiModelProperty("性别")
    private Integer genderType;
    @ApiModelProperty("生日")
    private String birthday;
}
