package io.github.aicyi.example.domain.entity.base;

import io.github.aicyi.commons.lang.model.BaseEntity;
import io.github.aicyi.commons.lang.type.BooleanType;
import io.github.aicyi.example.domain.type.GenderType;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表:t_user
 */
public class User extends BaseEntity implements Serializable {
    /**
     * 主键:id
     */
    private Long id;

    /**
     * 用户名:username
     */
    private String username;

    /**
     * 用户名密码:password
     */
    private String password;

    /**
     * 手机号:mobile
     */
    private String mobile;

    /**
     * 邮箱地址:email
     */
    private String email;

    /**
     * 用户昵称:nickname
     */
    private String nickname;

    /**
     * 身份证:id_card
     */
    private String idCard;

    /**
     * 年龄:age
     */
    private Integer age;

    /**
     * 性别，1:男；2：女；:gender_type
     */
    private GenderType genderType;

    /**
     * 生日:birthday
     */
    private LocalDate birthday;

    /**
     * 删除标记，0：未删除，1：已删除:deleted
     */
    private BooleanType deleted;

    /**
     * 版本:version
     */
    private Integer version;

    /**
     * 创建时间:create_time
     */
    private LocalDateTime createTime;

    /**
     * 更新时间:update_time
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public BooleanType getDeleted() {
        return deleted;
    }

    public void setDeleted(BooleanType deleted) {
        this.deleted = deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}