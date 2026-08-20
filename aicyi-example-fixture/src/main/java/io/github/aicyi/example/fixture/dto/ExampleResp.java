package io.github.aicyi.example.fixture.dto;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.github.aicyi.example.web.vo.StudentResp;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * @author Mr.Min
 * @description 示例类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@FieldNameConstants
public class ExampleResp extends BaseBean implements VoBean {
    private String uuid;
    private Integer idx;
    private Integer status;
    private String amount;
    private String score;
    private String date;
    private String localDate;
    private String dateTime;
    private String timestamp;
    private String season;
    private Integer week;
    private List<String> idList;
    private String user;
    private StudentResp student;
    private String nothing;
}
