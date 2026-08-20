package io.github.aicyi.example.domain;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.PageParam;
import io.github.aicyi.example.domain.type.GradeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 15:08
 **/
@Getter
@Setter
public class StudentQuery extends PageParam implements BoBean {
    private Long userIdEq;
    private GradeType gradeTypeEq;
    private LocalDateTime registerTimeStart;
    private LocalDateTime registerTimeEnd;
}
