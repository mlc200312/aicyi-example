package io.github.aicyi.example.fixture.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 学生类DTO
 * @date 2019-05-27
 **/
@Getter
@Setter
@ContentRowHeight(20)
@ColumnWidth(40)
public class StudentExcel extends BaseBean implements BoBean {
    @ExcelProperty("主键")
    @ColumnWidth // 自动列宽
    private String id;
    @ExcelProperty("年龄")
    @ColumnWidth // 自动列宽
    private Integer age;
    @ExcelProperty("身份证ID")
    @ColumnWidth // 自动列宽
    private String idCard;
    @ExcelProperty("用户名")
    @ColumnWidth // 自动列宽
    private String userName;
    @ExcelProperty("手机号")
    @ColumnWidth // 自动列宽
    private String mobile;
    @ExcelProperty("性别")
    @ColumnWidth // 自动列宽
    private Integer genderType;
    @ExcelProperty("生日")
    @ColumnWidth // 自动列宽
    private String birthday;
    @ExcelProperty("年级")
    @ColumnWidth // 自动列宽
    private String gradeType;
    @ExcelProperty("成绩")
    @ColumnWidth // 自动列宽
    private String score0;
    @ExcelProperty("注册时间")
    @ColumnWidth // 自动列宽
    private String registerTime;
    @ExcelProperty("创建时间")
    @ColumnWidth // 自动列宽
    private String createTime;
    @ExcelProperty("更新时间")
    @ColumnWidth // 自动列宽
    private String updateTime;
}
