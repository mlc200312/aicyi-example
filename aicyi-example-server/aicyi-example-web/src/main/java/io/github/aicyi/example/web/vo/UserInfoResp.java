package io.github.aicyi.example.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 用户信息响应参数
 * @date 17:10
 **/
@Getter
@Setter
public class UserInfoResp extends BaseBean implements VoBean {
    @JsonProperty("userId")
    @Schema(description = "用户ID")
    private String id;
    @JsonProperty("username")
    @Schema(description = "用户名")
    private String uniqueName;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "邮箱")
    private String email;
}
