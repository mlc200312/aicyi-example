package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.example.domain.type.GenderType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;

/**
 * @author Mr.Min
 * @description 用户类
 * @date 2019-05-14
 **/
@Getter
@Setter
@FieldNameConstants
public class UserBean extends BaseBean implements BoBean {
    private Long id;
    private String userName;
    private Integer age;
    private String idCard;
    private String mobile;
    private GenderType genderType;
    private LocalDate birthday;
}
