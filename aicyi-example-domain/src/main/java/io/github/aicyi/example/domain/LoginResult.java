package io.github.aicyi.example.domain;

import io.github.aicyi.commons.core.token.TokenPair;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 登录结果
 * @date 20:04
 **/
@Getter
@Setter
public class LoginResult extends BaseBean implements BoBean {
    private Long userId;
    private TokenPair token;
}
