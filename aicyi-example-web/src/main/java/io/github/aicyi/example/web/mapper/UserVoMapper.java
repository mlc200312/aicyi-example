package io.github.aicyi.example.web.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.web.vo.UpdateUserInfoReq;
import io.github.aicyi.example.web.vo.UserInfoResp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 用户模块 VO 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {EnumTypeConverters.class, DateTimeTypeConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserVoMapper {

    /**
     * 用户信息 -> 用户信息响应（仅同名属性映射，与原 Orika 行为一致）
     */
    UserInfoResp toUserInfoResp(UserInfo userInfo);

    /**
     * 更新用户信息请求 -> 用户实体（genderType / birthday 由 String 转换）
     */
    User toUser(UpdateUserInfoReq req);
}
