package io.github.aicyi.example.web;

import io.github.aicyi.commons.lang.model.Result;
import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.service.util.UserSessions;
import io.github.aicyi.example.web.vo.UserInfoResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.Min
 * @description 用户控制器
 * @date 15:45
 **/
@Api(value = "用户控制器", tags = {"用户控制器"})
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final BeanMapper beanMapper;

    @ApiOperation(value = "查询用户信息", notes = "查询用户信息")
    @ApiImplicitParam(
            name = "Authorization",
            value = "令牌",
            required = true,
            paramType = "header",
            dataTypeClass = String.class
    )
    @RequestMapping(value = "/get-user-info", method = RequestMethod.GET)
    public Result<UserInfoResp> getUserInfo() {

        UserInfo userInfo = UserSessions.getUserInfo();

        UserInfoResp resp = beanMapper.map(userInfo, UserInfoResp.class);

        return Result.success(resp);
    }
}
