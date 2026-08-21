package io.github.aicyi.example.mybaitsplus.service;

import io.github.aicyi.commons.util.bean.MapperUtils;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.fixture.util.DataSource;
import io.github.aicyi.example.mybatisplus.AicyiExampleMyBatisPlusApplication;
import io.github.aicyi.example.mybatisplus.entity.User;
import io.github.aicyi.example.mybatisplus.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 2026/8/21
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleMyBatisPlusApplication.class)
public class IUserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    public void save() {

        UserBean userBean = DataSource.getUser();

        User user = MapperUtils.getInstance().map(userBean, User.class);

        user.setUsername(userBean.getUserName());

        userService.save(user);
    }
}
