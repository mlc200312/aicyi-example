package io.github.aicyi.example.domain.mapper;

import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.domain.entity.base.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 用户信息映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 * <p>
 * {@code userId} / {@code deviceId} 等非同名字段由调用方（{@link UserInfo#of}）显式赋值，
 * 与原 Orika byDefault 同名映射行为保持一致。
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserInfoMapper {

    UserInfoMapper INSTANCE = Mappers.getMapper(UserInfoMapper.class);

    /**
     * User 实体 -> 用户信息（仅同名属性映射）
     */
    UserInfo toUserInfo(User user);
}
