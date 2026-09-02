package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.commons.core.token.TokenPair;
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
@Schema(description = "登录响应参数")
public class LoginResp implements VoBean {
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "令牌对")
    private TokenPair token;
}
