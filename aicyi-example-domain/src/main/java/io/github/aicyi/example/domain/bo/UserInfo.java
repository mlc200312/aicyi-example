package io.github.aicyi.example.domain.bo;

import io.github.aicyi.commons.core.token.IJWTInfo;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.domain.mapper.UserInfoMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mr.Min
 * @description 用户信息
 * @date 11:15
 **/
@Getter
@Setter
public class UserInfo extends BaseBean implements IJWTInfo, BoBean {
    private Long userId;
    private String username;
    private String nickname;
    private String mobile;
    private String deviceId;

    public static UserInfo of(User user) {
        UserInfo userInfo = UserInfoMapper.INSTANCE.toUserInfo(user);
        userInfo.setUserId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setMobile(user.getMobile());
        return userInfo;
    }

    @Override
    public String getId() {
        return String.valueOf(userId);
    }

    @Override
    public String getUniqueName() {
        return username;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }
}
