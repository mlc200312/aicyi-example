package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.PageParam;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 15:08
 **/
@Getter
@Setter
public class UserQuery extends PageParam implements BoBean {
    private String mobileEq;
    private String idCardEq;
    private LocalDate birthdayStart;
    private LocalDate birthdayEnd;
    private List<Long> idListIn;
}
