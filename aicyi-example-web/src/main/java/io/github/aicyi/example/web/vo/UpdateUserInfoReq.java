package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.example.domain.type.GenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * @author Mr.Min
 * @description 注册请求参数
 * @date 15:05
 **/
@Getter
@Setter
@Schema(description = "更新用户信息请求参数")
public class UpdateUserInfoReq extends BaseBean implements VoBean {
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "邮箱地址")
    private String email;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "身份证")
    private String idCard;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "性别")
    private Integer genderType;
    @Schema(description = "生日")
    private String birthday;
}
