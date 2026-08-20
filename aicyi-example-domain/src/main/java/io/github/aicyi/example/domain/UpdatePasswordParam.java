package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 修改密码参数
 * @date 12:00
 **/
@Getter
@Setter
public class UpdatePasswordParam extends BaseBean implements BoBean {
    private String username;
    private String newPassword;
    private String uuid;
    private String verCode;
}
