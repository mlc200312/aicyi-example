package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Mr.Min
 * @description 刷新令牌请求参数
 * @date 21:47
 **/
@Getter
@Setter
@ApiModel("刷新令牌请求参数")
public class RefreshTokenReq extends BaseBean implements VoBean {

    @NotBlank
    @ApiModelProperty("刷新令牌")
    private String refreshToken;
}
