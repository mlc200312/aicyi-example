package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Mr.Min
 * @description 添加学生
 * @date 12:04
 **/
@Getter
@Setter
@Schema(description = "新增学生请求参数")
public class AddStudentReq extends BaseBean implements VoBean {
    @NotBlank
    @Schema(description = "身份证号")
    private String idCard;
    @NotBlank
    @Schema(description = "班级")
    private String gradeType;
}
