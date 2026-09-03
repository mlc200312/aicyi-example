package io.github.aicyi.example.domain.bo;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import lombok.Setter;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * @author Mr.Min
 * @description 消息类
 * @date 2019-06-24
 **/
@Setter
@XmlRootElement(name = "xml")
public class Message extends BaseBean implements BoBean {
    private String toUserName;
    private String fromUserName;
    private Long createTime;
    private String msgType;
    private String content;
    private Long msgId;

    @XmlAttribute(name = "ToUserName")
    public String getToUserName() {
        return toUserName;
    }

    @XmlAttribute(name = "FromUserName")
    public String getFromUserName() {
        return fromUserName;
    }

    @XmlAttribute(name = "CreateTime")
    public Long getCreateTime() {
        return createTime;
    }

    @XmlAttribute(name = "MsgType")
    public String getMsgType() {
        return msgType;
    }

    @XmlAttribute(name = "Content")
    public String getContent() {
        return content;
    }

    @XmlAttribute(name = "MsgId")
    public Long getMsgId() {
        return msgId;
    }

}
