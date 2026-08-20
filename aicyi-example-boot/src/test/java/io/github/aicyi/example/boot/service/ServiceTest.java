package io.github.aicyi.example.boot.service;

import io.github.aicyi.commons.util.DateTimeUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.UserQuery;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.service.StudentService;
import io.github.aicyi.example.service.UserService;
import io.github.aicyi.midware.db.commons.PageUtils;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.example.fixture.util.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 15:32
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class ServiceTest extends BaseLoggerTest {

    @Autowired
    private UserService userService;
    @Autowired
    private StudentService studentService;

    private String testMobile;
    private StudentBean studentBean;


    @Before
    @Override
    public void beforeTest() {
        testMobile = "15910436675";
        studentBean = DataSource.getStudent();
        studentBean.setMobile(testMobile);
        studentBean.setIdCard("1f0a9a831f9e6ec3817d77e5fd2ca3bb");
        studentBean.setBirthday(DateTimeUtils.parseAuto("2025-10-11 00:00:00.000").toLocalDate());
    }

    @Test
    @Override
    public void test() {
        studentService.add(studentBean);
        StudentBean student = studentService.getByMobile(studentBean.getMobile());
        User user = userService.getById(student.getId());

        log(user, student);
    }

    @Test
    public void test0() {
        List<StudentBean> studentBeanList = DataSource.getStudentList();
        studentBeanList.forEach(item -> studentService.add(item));
    }

    @Test
    public void test1() {
        StudentBean student = studentService.getByMobile(testMobile);
        studentService.delete(student.getId());

        log(student);
    }

    @Test
    public void test2() {
        StudentBean student = studentService.getByMobile(testMobile);
        if (Objects.nonNull(student)) {
            student.setScore(new BigDecimal(100).doubleValue());
            studentService.update(student);
        }
    }

    @Test
    public void test3() {
        UserQuery query = new UserQuery();
        query.setMobileEq(testMobile);
        query.setIdCardEq("1f0a9a831f9e6ec3817d77e5fd2ca3bb");
        query.setBirthdayStart(DateTimeUtils.parseAuto("2025-10-01 00:00:00.000").toLocalDate());
        query.setBirthdayEnd(DateTimeUtils.parseAuto("2025-11-01 00:00:00.000").toLocalDate());

        Pageable pageable = PageUtils.createPageable(1, 10, Sort.by(Sort.Order.desc("update_time"), Sort.Order.asc("id")));
        List<User> list = userService.list(pageable, query);

        log(list);
    }

    @Test
    public void test30() {
        UserQuery query = new UserQuery();
        query.setBirthdayStart(DateTimeUtils.parseAuto("2024-10-01 00:00:00.000").toLocalDate());
        query.setBirthdayEnd(DateTimeUtils.parseAuto("2026-11-01 00:00:00.000").toLocalDate());

        Pageable pageable = PageUtils.createPageable(1, 10, Sort.by(Sort.Order.desc("update_time"), Sort.Order.asc("id")));
        Page<User> pageResult = userService.pagedList(pageable, query);

        log(pageResult.getContent(), pageResult.getTotalPages());
    }
}
