package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.example.domain.type.CaptchaType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 18:19
 **/
@Getter
@Setter
public class SendCaptchaParam extends BaseBean implements BoBean {
    private CaptchaType captchaType;
    private String username;
    private String uuid;
    private String verCode;
}
