package io.github.aicyi.example.domain.bo;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.example.domain.type.GradeType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 19:21
 **/
@Getter
@Setter
public class AddStudentParam extends BaseBean implements BoBean {

    private Long userId;

    private BigDecimal score;

    private GradeType gradeType;
}
