package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.commons.core.token.TokenPair;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 学生类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@ApiModel("登录响应参数")
public class LoginResp implements VoBean {
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("令牌对")
    private TokenPair token;
}
