package io.github.aicyi.example.dao.mapper;

import io.github.aicyi.example.domain.entity.base.Student;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 16:25
 **/
public interface StudentCustomMapper {

    @Select({
            "select t1.id,t1.user_id,t1.score,t1.grade_type,t1.register_time,t1.deleted,t1.version,t1.create_time,t1.update_time",
            "from t_student t1",
            "inner join t_user t2 on t1.user_id = t2.id",
            "where t2.mobile = #{mobile}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.BIGINT),
            @Result(column = "score", property = "score", jdbcType = JdbcType.DECIMAL),
            @Result(column = "grade_type", property = "gradeType", jdbcType = JdbcType.VARCHAR),
            @Result(column = "register_time", property = "registerTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "deleted", property = "deleted", jdbcType = JdbcType.TINYINT),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP)
    })
    Student selectByMobile(String mobile);
}
