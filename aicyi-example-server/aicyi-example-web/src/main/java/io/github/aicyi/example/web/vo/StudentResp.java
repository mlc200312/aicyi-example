package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 学生类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@Schema(description = "学生对象响应参数")
public class StudentResp extends BaseBean implements VoBean {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "身份证ID")
    private String idCard;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "性别")
    private Integer genderType;
    @Schema(description = "生日")
    private String birthday;
    @Schema(description = "班级")
    private String gradeType;
    @Schema(description = "成绩")
    private String score0;
    @Schema(description = "注册时间")
    private String registerTime;
    @Schema(description = "创建时间")
    private String createTime;
    @Schema(description = "更新时间")
    private String updateTime;
}
