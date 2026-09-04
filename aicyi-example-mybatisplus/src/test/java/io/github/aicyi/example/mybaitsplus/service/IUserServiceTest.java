package io.github.aicyi.example.mybaitsplus.service;

import io.github.aicyi.example.domain.bo.UserBean;
import io.github.aicyi.example.domain.util.DataSource;
import io.github.aicyi.example.mybatisplus.AicyiExampleMyBatisPlusApplication;
import io.github.aicyi.example.mybatisplus.domain.entity.User;
import io.github.aicyi.example.mybaitsplus.mapper.UserBeanMapper;
import io.github.aicyi.example.mybatisplus.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 2026/8/21
 **/
@SpringBootTest(classes = AicyiExampleMyBatisPlusApplication.class)
public class IUserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    public void save() {

        UserBean userBean = DataSource.getUser();

        User user = UserBeanMapper.INSTANCE.toUser(userBean);

        user.setUsername(userBean.getUserName());

        userService.save(user);
    }

    @Test
    public void update() {

        User user = userService.getById(349144798063493120L);

        user.setPassword("update");

        userService.updateById(user);
    }
}
