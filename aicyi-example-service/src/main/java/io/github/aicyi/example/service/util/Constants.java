package io.github.aicyi.example.service.util;

import io.github.aicyi.commons.core.message.MessageContent;
import io.github.aicyi.commons.lang.Assert;
import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.example.domain.type.CaptchaType;
import io.github.aicyi.midware.message.mail.model.MailMessage;
import io.github.aicyi.midware.message.sms.model.SmsMessage;

import java.util.Map;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 16:07
 **/
public class Constants {

    public static String getCaptchaKey(String uuid) {
        return String.format("captcha:", uuid);
    }

    public static String getCaptchaKey(CaptchaType captchaType, String uuid) {
        switch (captchaType) {
            case IMAGE_CAPTCHA_TYPE:
                return getCaptchaKey(uuid);
            case LOGIN_CAPTCHA_TYPE:
                return String.format("email:captcha:login:", uuid);
            case REGISTER_CAPTCHA_TYPE:
                return String.format("email:captcha:register:", uuid);
            case UPDATE_PASSWORD_CAPTCHA_TYPE:
                return String.format("email:captcha:update_password:", uuid);
            default:
                throw new IllegalArgumentException("captchaType is not support");
        }
    }

    public static MessageContent<String> getEmailMessageContent(CaptchaType captchaType, String captcha, String email) {
        Map<String, Object> templateParams = Maps
                .ofStr("code", captcha)
                .and("expireMinutes", "10")
                .build();
        switch (captchaType) {
            case LOGIN_CAPTCHA_TYPE:
                return MailMessage.of(email, "SMS_LOGIN_CODE", templateParams);
            case REGISTER_CAPTCHA_TYPE:
                return MailMessage.of(email, "SMS_REGISTER_CODE", templateParams);
            default:
                throw new IllegalArgumentException("captchaType is not support");
        }
    }

    public static MessageContent<String> getSmsMessageContent(CaptchaType captchaType, String captcha, String phone) {
        Assert.notNull(captchaType, "captchaType");
        if (captchaType == CaptchaType.UPDATE_PASSWORD_CAPTCHA_TYPE) {
            Map<String, Object> templateParams = Maps
                    .ofStr("code", captcha)
                    .and("expireMinutes", "10")
                    .build();
            return SmsMessage.of(phone, "SMS_LOGIN_CODE", templateParams);
        }
        throw new IllegalArgumentException("captchaType is not support");
    }
}
