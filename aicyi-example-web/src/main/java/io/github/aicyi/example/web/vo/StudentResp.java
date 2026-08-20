package io.github.aicyi.example.web.vo;

import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.VoBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 学生类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@ApiModel("学生对象响应参数")
public class StudentResp extends BaseBean implements VoBean {
    @ApiModelProperty("主键ID")
    private String id;
    @ApiModelProperty("年龄")
    private Integer age;
    @ApiModelProperty("身份证ID")
    private String idCard;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("性别")
    private Integer genderType;
    @ApiModelProperty("生日")
    private String birthday;
    @ApiModelProperty("班级")
    private String gradeType;
    @ApiModelProperty("成绩")
    private String score0;
    @ApiModelProperty("注册时间")
    private String registerTime;
    @ApiModelProperty("创建时间")
    private String createTime;
    @ApiModelProperty("更新时间")
    private String updateTime;
}
