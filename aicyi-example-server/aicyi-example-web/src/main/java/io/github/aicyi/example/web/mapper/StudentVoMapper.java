package io.github.aicyi.example.web.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.bo.AddStudentParam;
import io.github.aicyi.example.domain.bo.StudentBean;
import io.github.aicyi.example.domain.bo.StudentQuery;
import io.github.aicyi.example.web.dto.AddStudentReq;
import io.github.aicyi.example.web.dto.StudentReq;
import io.github.aicyi.example.web.vo.StudentResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 学生模块 VO 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {EnumTypeConverters.class, DateTimeTypeConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface StudentVoMapper {

    /**
     * 分页查询请求 -> 查询参数（userIdEq / 时间区间由 String 转换）
     */
    StudentQuery toStudentQuery(StudentReq req);

    /**
     * 新增学生请求 -> 学生 Bean（gradeType 由 String 转枚举）
     */
    AddStudentParam toAddStudentParam(AddStudentReq req);

    /**
     * 学生 Bean -> 学生响应（score 映射为 score0，时间/枚举转 String）
     */
    @Mapping(source = "score", target = "score0")
    StudentResp toStudentResp(StudentBean bean);
}
