package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.example.domain.type.GenderType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author Mr.Min
 * @description 注册参数
 * @date 14:26
 **/
@Getter
@Setter
public class RegisterParam extends BaseBean implements BoBean {
    private String username;
    private String password;
    private String mobile;
    private LocalDate birthday;
    private GenderType genderType;
    private String verCode;
}
