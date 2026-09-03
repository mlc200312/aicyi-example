package io.github.aicyi.example.service;

import io.github.aicyi.example.domain.bo.AddStudentParam;
import io.github.aicyi.example.domain.bo.StudentBean;
import io.github.aicyi.example.domain.bo.StudentQuery;
import io.github.aicyi.example.domain.entity.base.Student;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Mr.Min
 * @description 用户服务
 * @date 14:51
 **/
public interface StudentService {

    Long add(AddStudentParam userId);

    void delete(Long id);

    void update(StudentBean bean);

    StudentBean getById(Long id);

    StudentBean getByUserId(Long userId);

    StudentBean getByMobile(String mobile);

    List<Student> list(StudentQuery query);

    Page<StudentBean> pagedList(StudentQuery query);
}
