package io.github.aicyi.example.domain;

import io.github.aicyi.example.domain.type.GradeType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * @author Mr.Min
 * @description 学生类
 * @date 2019-05-21
 **/
@Getter
@Setter
@FieldNameConstants
public class StudentBean extends UserBean {
    private Long studentId;
    private Double score;
    private GradeType gradeType;
    private LocalDateTime registerTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
