package io.github.aicyi.example.service.util;

import io.github.aicyi.commons.core.cache.Cache;
import io.github.aicyi.commons.util.context.CurrentContextHolder;
import io.github.aicyi.example.domain.bo.UserInfo;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Min
 * @description 当前登录用户会话工具：基于当前线程上下文的用户ID，从用户信息缓存读取当前用户，
 * 未命中时回源数据库。通过 InitializingBean 将 Spring 实例发布为静态句柄，对外保留静态便捷调用
 * @date 16:29
 **/
@Component
@RequiredArgsConstructor
public class UserSessions implements InitializingBean {

    /**
     * volatile 保证 Spring 容器初始化后对其他线程的安全可见
     */
    private static volatile UserSessions INSTANCE;

    /**
     * final 字段配合 @RequiredArgsConstructor 实现构造器注入；
     * 非 final 时 Lombok 不会生成构造参数，字段将始终为 null
     */
    private final Cache<String, UserInfo> userInfoRedisCache;
    private final UserService userService;

    /**
     * 获取当前登录用户ID
     *
     * @return 未登录、userId 非法时抛异常
     */
    public static Long getUserId() {

        String userId = CurrentContextHolder.getUserId();

        // isNumeric 前置拦截非法 userId，避免 Long.valueOf 抛 NumberFormatException
        if (StringUtils.isBlank(userId) || !StringUtils.isNumeric(userId)) {
            throw new IllegalArgumentException("Illegal userId: " + userId);
        }

        return Long.valueOf(userId);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 未登录、userId 非法或用户不存在时返回 null
     */
    public static UserInfo getUserInfo() {

        UserSessions sessions = INSTANCE;
        if (sessions == null) {
            throw new IllegalStateException("UserSessions has not been initialized by Spring container");
        }

        Long userId = getUserId();

        return sessions.userInfoRedisCache.get(String.valueOf(userId), key -> {

            User user = sessions.userService.getById(userId);

            // 用户不存在（如已注销）返回 null，由缓存层按 cacheNull 配置决定空值防穿透
            return user == null ? null : UserInfo.of(user);
        });
    }

    @Override
    public void afterPropertiesSet() {
        INSTANCE = this;
    }
}
