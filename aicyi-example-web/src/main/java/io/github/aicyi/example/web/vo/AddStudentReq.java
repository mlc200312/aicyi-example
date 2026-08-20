package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author Mr.Min
 * @description 添加学生
 * @date 12:04
 **/
@Getter
@Setter
@ApiModel("新增学生请求参数")
public class AddStudentReq extends BaseBean implements VoBean {
    @NotBlank
    @ApiModelProperty("身份证号")
    private String idCard;
    @NotBlank
    @ApiModelProperty("班级")
    private String gradeType;
}
