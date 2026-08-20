package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.util.orikamapper.*;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.entity.base.Student;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.web.vo.StudentResp;
import io.github.aicyi.example.fixture.domain.Example;
import io.github.aicyi.example.fixture.domain.ExampleBean;
import io.github.aicyi.example.fixture.dto.ExampleResp;
import ma.glasnost.orika.MapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:09
 **/
@Configuration
public class ManagerConfiguration {
    @Bean
    public BeanMapper smartMapper() {
        OrikaMapper orikaMapper = new OrikaMapper();
        MapperFactory mapperFactory = orikaMapper.getMapperFactory();

        example2ExampleRespClassMapBuilder(mapperFactory);
        example2ExampleBeanClassMapBuilder(mapperFactory);
        studentBean2UserClassMapBuilder(mapperFactory);
        studentBean2StudentRespClassMapBuilder(mapperFactory);
        studentBean2StudentClassMapBuilder(mapperFactory);
        student2StudentBeanClassMapBuilder(mapperFactory);

        return orikaMapper;
    }

    private void example2ExampleRespClassMapBuilder(MapperFactory factory) {
        factory.classMap(Example.class, ExampleResp.class)
                .field("student.score", "student.score0")
                .exclude("nothing")
                .byDefault().register();
    }

    private void example2ExampleBeanClassMapBuilder(MapperFactory factory) {
        factory.classMap(Example.class, ExampleBean.class)
                .exclude("nothing")
                .byDefault().register();
    }

    private void studentBean2UserClassMapBuilder(MapperFactory factory) {
        factory.classMap(StudentBean.class, User.class)
                .field("userName", "username")
                .byDefault().register();
    }

    private void studentBean2StudentRespClassMapBuilder(MapperFactory factory) {
        factory.classMap(StudentBean.class, StudentResp.class)
                .field("score", "score0")
                .byDefault().register();
    }

    private void studentBean2StudentClassMapBuilder(MapperFactory factory) {
        factory.classMap(StudentBean.class, Student.class)
                .field("studentId", "id")
                .byDefault().register();
    }

    private void student2StudentBeanClassMapBuilder(MapperFactory factory) {
        factory.classMap(Student.class, StudentBean.class)
                .field("id", "studentId")
                .field("userId", "id")
                .byDefault().register();
    }
}
