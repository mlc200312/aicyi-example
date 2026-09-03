package io.github.aicyi.example.service.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.bo.AddStudentParam;
import io.github.aicyi.example.domain.bo.RegisterParam;
import io.github.aicyi.example.domain.bo.StudentBean;
import io.github.aicyi.example.domain.entity.base.Student;
import io.github.aicyi.example.domain.entity.base.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * 业务 Bean 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 * <p>
 * 覆盖 AuthServiceImpl / StudentServiceImpl 的实体转换场景；非同名字段
 * （如 {@code userName -> username}、{@code studentId -> userId}）与原 Orika
 * byDefault 同名映射行为保持一致，由调用方显式赋值。
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {EnumTypeConverters.class, DateTimeTypeConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ServiceBeanMapper {

    /**
     * 注册参数 -> 用户实体
     */
    User toUser(RegisterParam param);

    /**
     * 添加学生参数 -> 学生实体
     *
     */
    Student toStudent(AddStudentParam param);

    /**
     * 学生 Bean -> 学生实体
     */
    Student toStudent(StudentBean bean);

    /**
     * 学生实体 -> 学生 Bean
     */
    StudentBean toStudentBean(User user);

    /**
     * 用户实体 -> 学生 Bean（原地填充同名属性，userId 由调用方赋值）
     */
    void updateStudentBean(Student student, @MappingTarget StudentBean studentBean);
}
