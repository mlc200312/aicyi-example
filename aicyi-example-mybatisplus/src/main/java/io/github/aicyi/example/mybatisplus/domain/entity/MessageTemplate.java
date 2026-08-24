package io.github.aicyi.example.mybatisplus.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serializable;
import java.lang.Boolean;
import java.time.LocalDateTime;

import io.github.aicyi.midware.db.mybatisplus.handlers.StringListTypeHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 消息模版表
 * </p>
 *
 * @author Leno
 * @since 2026-08-24
 */
@Getter
@Setter
@TableName(value = "message_template", autoResultMap = true)
public class MessageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模版编码
     */
    private String templateCode;

    /**
     * 模版名称
     */
    private String templateName;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 模版格式
     */
    private String format;

    /**
     * 模版引擎类型
     */
    private String engineType;

    /**
     * 模版主题
     */
    private String subject;

    /**
     * 模版内容
     */
    private String content;

    /**
     * 短信签名
     */
    private String signature;

    /**
     * 模版参数
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private String variables;

    /**
     * 是否启用，0:未启用，1:已启用；
     */
    private Byte enabled;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记，0：未删除，1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deleted;

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
