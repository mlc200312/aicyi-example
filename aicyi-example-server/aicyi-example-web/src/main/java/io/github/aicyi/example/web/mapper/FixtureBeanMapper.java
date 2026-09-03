package io.github.aicyi.example.web.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.bo.ExampleBean;
import io.github.aicyi.example.domain.bo.StudentBean;
import io.github.aicyi.example.domain.bo.UserBean;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.web.vo.ExampleResp;
import io.github.aicyi.example.web.vo.StudentResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 测试数据 Bean 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 * <p>
 * 覆盖 DataSource 的 Mock 数据转换场景；字段映射与原 Orika 配置等价：
 * <ul>
 *     <li>StudentBean -&gt; StudentResp：score 映射为 score0</li>
 *     <li>ExampleBean -&gt; ExampleResp：id 映射为 uuid，忽略 user / student</li>
 *     <li>UserBean -&gt; User：仅同名属性（userName 与 username 不同名，不映射）</li>
 * </ul>
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(uses = {EnumTypeConverters.class, DateTimeTypeConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface FixtureBeanMapper {

    FixtureBeanMapper INSTANCE = Mappers.getMapper(FixtureBeanMapper.class);

    /**
     * 用户 Bean -> 学生 Bean（StudentBean 继承 UserBean，同名属性直接映射）
     */
    StudentBean toStudentBean(UserBean user);

    /**
     * 用户 Bean -> 学生 Bean（原地填充同名属性）
     */
    void updateStudentBean(UserBean user, @MappingTarget StudentBean student);

    /**
     * 学生 Bean -> 学生响应（score 映射为 score0，时间/枚举转 String）
     */
    @Mapping(source = "score", target = "score0")
    StudentResp toStudentResp(StudentBean student);

    /**
     * 示例 Bean -> 示例响应（id 映射为 uuid，忽略 user / student）
     */
    @Mapping(source = "id", target = "uuid")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "student", ignore = true)
    ExampleResp toExampleResp(ExampleBean example);

    /**
     * 用户 Bean -> 用户实体（仅同名属性映射，与原 Orika 行为一致）
     */
    User toUser(UserBean user);
}
