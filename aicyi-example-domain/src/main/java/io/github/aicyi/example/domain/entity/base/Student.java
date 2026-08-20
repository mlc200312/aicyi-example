package io.github.aicyi.example.domain.entity.base;

import io.github.aicyi.commons.lang.model.BaseEntity;
import io.github.aicyi.commons.lang.type.BooleanType;
import io.github.aicyi.example.domain.type.GradeType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生表:t_student
 */
public class Student extends BaseEntity implements Serializable {
    /**
     * 主键:id
     */
    private Long id;

    /**
     * 用户ID:user_id
     */
    private Long userId;

    /**
     * 成绩:score
     */
    private BigDecimal score;

    /**
     * 年级:grade_type
     */
    private GradeType gradeType;

    /**
     * 注册时间:register_time
     */
    private LocalDateTime registerTime;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public GradeType getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeType gradeType) {
        this.gradeType = gradeType;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
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