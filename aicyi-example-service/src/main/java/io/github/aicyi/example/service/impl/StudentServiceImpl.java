package io.github.aicyi.example.service.impl;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.core.id.IdGenerator;
import io.github.aicyi.commons.lang.exception.BusinessException;
import io.github.aicyi.commons.lang.type.BooleanType;
import io.github.aicyi.commons.util.NumberUtils;
import io.github.aicyi.example.dao.mapper.StudentCustomMapper;
import io.github.aicyi.example.dao.mapper.base.StudentMapper;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.StudentQuery;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.domain.UserQuery;
import io.github.aicyi.example.domain.entity.base.Student;
import io.github.aicyi.example.domain.entity.base.StudentExample;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.domain.type.ExampleResultCode;
import io.github.aicyi.example.service.StudentService;
import io.github.aicyi.example.service.UserService;
import io.github.aicyi.example.service.util.UserSessions;
import io.github.aicyi.midware.db.commons.BaseEntityUtils;
import io.github.aicyi.midware.db.commons.PageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:52
 **/
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final BeanMapper beanMapper;
    private final IdGenerator idGenerator;
    private final StudentMapper studentMapper;
    private final StudentCustomMapper studentCustomMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void add(StudentBean bean) {
        UserInfo userInfo = UserSessions.getUserInfo();
        StudentBean student = getByUserId(userInfo.getUserId());
        if (Objects.nonNull(student)) {
            return;
        }
        Student newStudent = beanMapper.map(bean, Student.class);
        newStudent.setUserId(userInfo.getUserId());
        newStudent.setRegisterTime(LocalDateTime.now());
        BaseEntityUtils.setDefaultValue(newStudent, idGenerator);
        studentMapper.insertSelective(newStudent);
    }

    @Override
    public void delete(Long id) {
        studentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(StudentBean bean) {
        Student student = beanMapper.map(bean, Student.class);
        studentMapper.updateByPrimaryKeySelective(student);
    }

    @Override
    public StudentBean getById(Long id) {
        Student student = studentMapper.selectByPrimaryKey(id);
        if (Objects.isNull(student)) {
            throw new BusinessException(ExampleResultCode.OBJECT_NOT_FOUND);
        }
        User user = userService.getById(student.getUserId());
        return createStudentBean(student, user);
    }

    @Override
    public StudentBean getByUserId(Long userId) {
        StudentExample example = new StudentExample();
        StudentExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(BooleanType.FALSE);
        criteria.andUserIdEqualTo(userId);
        List<Student> studentList = studentMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(studentList)) {
            throw new BusinessException(ExampleResultCode.OBJECT_NOT_FOUND);
        }
        Student student = studentList.get(0);
        User user = userService.getById(student.getUserId());
        return createStudentBean(student, user);
    }

    @Override
    public StudentBean getByMobile(String mobile) {
        Student student = studentCustomMapper.selectByMobile(mobile);
        if (Objects.isNull(student)) {
            throw new BusinessException(ExampleResultCode.OBJECT_NOT_FOUND);
        }
        User user = userService.getById(student.getUserId());
        return createStudentBean(student, user);
    }

    @Override
    public List<Student> list(StudentQuery query) {
        StudentExample studentExample = new StudentExample();
        StudentExample.Criteria criteria = studentExample.createCriteria();
        if (Objects.isNull(query)) {
            return Collections.emptyList();
        }
        if (NumberUtils.isPositive(query.getUserIdEq())) {
            criteria.andUserIdEqualTo(query.getUserIdEq());
        }
        if (Objects.nonNull(query.getGradeTypeEq())) {
            criteria.andGradeTypeEqualTo(query.getGradeTypeEq());
        }
        if (Objects.nonNull(query.getRegisterTimeStart())) {
            criteria.andRegisterTimeGreaterThan(query.getRegisterTimeStart());
        }
        if (Objects.nonNull(query.getRegisterTimeEnd())) {
            criteria.andRegisterTimeLessThanOrEqualTo(query.getRegisterTimeEnd());
        }
        List<Student> studentList = studentMapper.selectByExample(studentExample);

        return studentList;
    }

    @Override
    public Page<StudentBean> pagedList(StudentQuery query) {

        Page<Student> page = PageUtils.getPage(query, () -> list(query));

        if (!page.isEmpty()) {
            List<Student> studentList = page.getContent();
            UserQuery userQuery = new UserQuery();
            List<Long> userIdList = studentList.stream().map(Student::getUserId).collect(Collectors.toList());
            userQuery.setIdListIn(userIdList);

            List<User> userList = userService.list(userQuery);
            Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, o -> o));

            return page.map(o -> createStudentBean(o, userMap.get(o.getUserId())));
        }

        return Page.empty(page.getPageable());
    }

    private StudentBean createStudentBean(Student student, User user) {
        StudentBean studentBean = beanMapper.map(student, StudentBean.class);
        beanMapper.map(user, studentBean);
        return studentBean;
    }
}
