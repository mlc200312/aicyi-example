package io.github.aicyi.example.fixture.domain;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.type.BooleanType;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.domain.type.Season;
import io.github.aicyi.example.domain.type.Week;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.Min
 * @description 示例类
 * @date 2019-05-21
 **/
@Getter
@Setter
@FieldNameConstants
public class Example extends BaseBean implements BoBean {
    private Long id;
    private Integer idx;
    private BooleanType status;
    private BigDecimal amount;
    private Double score;
    private Date date;
    private LocalDate localDate;
    private LocalDateTime dateTime;
    private Timestamp timestamp;
    private Season season;
    private Week week;
    private List<Long> idList;
    private UserBean user;
    private StudentBean student;
    private String nothing;
}
