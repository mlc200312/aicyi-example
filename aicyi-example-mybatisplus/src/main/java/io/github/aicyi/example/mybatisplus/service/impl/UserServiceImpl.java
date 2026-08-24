package io.github.aicyi.example.mybatisplus.service.impl;

import io.github.aicyi.example.mybatisplus.domain.entity.User;
import io.github.aicyi.example.mybatisplus.mapper.UserMapper;
import io.github.aicyi.example.mybatisplus.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Leno
 * @since 2026-08-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
