package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
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
@Schema(description = "注册请求参数")
public class RegisterReq extends BaseBean implements VoBean {
    @NotBlank
    @Schema(description = "用户名")
    private String username;
    @NotBlank
    @Schema(description = "密码")
    private String password;
    @NotBlank
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "生日")
    private LocalDate birthday;
    @Schema(description = "性别")
    private Integer genderType;
}
