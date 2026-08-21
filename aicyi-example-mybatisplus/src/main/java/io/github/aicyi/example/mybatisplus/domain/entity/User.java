package io.github.aicyi.example.mybatisplus.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.github.aicyi.example.mybatisplus.domain.type.GenderType;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Leno
 * @since 2026-08-21
 */
@Getter
@Setter
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户名密码
     */
    private String password;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别，1:男；2：女；
     */
    private GenderType genderType;

    /**
     * 生日
     */
    private java.time.LocalDate birthday;

    /**
     * 删除标记，0：未删除，1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private java.lang.Boolean deleted;

    /**
     * 版本
     */
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
