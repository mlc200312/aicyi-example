package io.github.aicyi.example.web.mapper;

import io.github.aicyi.commons.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.commons.util.bean.mapstruct.EnumTypeConverters;
import io.github.aicyi.example.domain.LoginParam;
import io.github.aicyi.example.domain.LoginResult;
import io.github.aicyi.example.domain.RegisterParam;
import io.github.aicyi.example.domain.UpdatePasswordParam;
import io.github.aicyi.example.web.vo.LoginReq;
import io.github.aicyi.example.web.vo.LoginResp;
import io.github.aicyi.example.web.vo.RegisterReq;
import io.github.aicyi.example.web.vo.UpdatePasswordReq;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 授权模块 VO 映射器（MapStruct 编译期生成，取代原 Orika 运行时反射映射）
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {EnumTypeConverters.class, DateTimeTypeConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AuthVoMapper {

    /**
     * 注册请求 -> 注册参数（genderType 由 Integer code 转枚举）
     */
    RegisterParam toRegisterParam(RegisterReq req);

    /**
     * 登录请求 -> 登录参数
     */
    LoginParam toLoginParam(LoginReq req);

    /**
     * 更新密码请求 -> 更新密码参数
     */
    UpdatePasswordParam toUpdatePasswordParam(UpdatePasswordReq req);

    /**
     * 登录结果 -> 登录响应（userId 由 Long 转 String）
     */
    LoginResp toLoginResp(LoginResult result);
}
