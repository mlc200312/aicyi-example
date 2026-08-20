package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 登录参数
 * @date 11:50
 **/
@Getter
@Setter
public class LoginParam extends BaseBean implements BoBean {
    private String username;
    private String password;
    private String uuid;
    private String verCode;
}
