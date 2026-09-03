package io.github.aicyi.example.web;

import io.github.aicyi.commons.lang.model.Result;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.service.UserService;
import io.github.aicyi.example.service.util.UserSessions;
import io.github.aicyi.example.web.mapper.UserVoMapper;
import io.github.aicyi.example.web.dto.UpdateUserInfoReq;
import io.github.aicyi.example.web.vo.UserInfoResp;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.Min
 * @description 用户控制器
 * @date 15:45
 **/
@Tag(name = "用户控制器")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserVoMapper beanMapper;
    private final UserService userService;

    @Operation(summary = "查询用户信息", description = "查询用户信息")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/get-user-info", method = RequestMethod.GET)
    public Result<UserInfoResp> getUserInfo() {
        User user = userService.getById(UserSessions.getUserId());
        UserInfoResp resp = beanMapper.toUserInfoResp(user);
        return Result.success(resp);
    }

    @Operation(summary = "更新用户信息", description = "更新用户信息")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/update-user-info", method = RequestMethod.POST)
    public Result<Void> updateUserInfo(@RequestBody UpdateUserInfoReq req) {
        User user = beanMapper.toUser(req);
        Long userId = UserSessions.getUserId();
        user.setId(userId);
        userService.update(user);
        return Result.success();
    }
}
