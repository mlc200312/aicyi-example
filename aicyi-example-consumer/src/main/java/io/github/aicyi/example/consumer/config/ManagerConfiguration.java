package io.github.aicyi.example.consumer.config;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.util.orikamapper.OrikaMapper;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.entity.base.Student;
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

        studentBean2StudentClassMapBuilder(mapperFactory);
        student2StudentBeanClassMapBuilder(mapperFactory);

        return orikaMapper;
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
