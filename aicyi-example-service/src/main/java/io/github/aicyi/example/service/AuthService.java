package io.github.aicyi.example.service;

import io.github.aicyi.example.domain.LoginParam;
import io.github.aicyi.example.domain.LoginResult;
import io.github.aicyi.example.domain.RegisterParam;
import io.github.aicyi.example.domain.UpdatePasswordParam;

/**
 * @author Mr.Min
 * @description 业务权限管理业务层
 * @date 20:03
 **/
public interface AuthService {

    /**
     * 注册
     *
     * @param param
     */
    void register(RegisterParam param);

    /**
     * 登录
     *
     * @param param
     * @return
     */
    LoginResult login(LoginParam param);

    /**
     * 修改密码
     *
     * @param param
     */
    void updatePassword(UpdatePasswordParam param);
}
