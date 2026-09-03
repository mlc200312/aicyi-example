package io.github.aicyi.example.mybaitsplus.mapper;

import io.github.aicyi.example.domain.bo.UserBean;
import io.github.aicyi.example.mybatisplus.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 用户 Bean 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 * <p>
 * 仅用于测试场景：UserBean -> mybatisplus User 实体，仅同名属性映射
 * （userName 与 username 不同名不映射，由调用方显式赋值）。
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserBeanMapper {

    UserBeanMapper INSTANCE = Mappers.getMapper(UserBeanMapper.class);

    /**
     * 用户 Bean -> mybatisplus 用户实体
     */
    User toUser(UserBean userBean);

    /**
     * domain GenderType -> mybatisplus GenderType（按枚举名匹配，与原 Orika 行为一致）
     */
    default io.github.aicyi.example.mybatisplus.domain.type.GenderType mapGenderType(
            io.github.aicyi.example.domain.type.GenderType value) {
        return value == null ? null
                : io.github.aicyi.example.mybatisplus.domain.type.GenderType.valueOf(value.name());
    }
}
