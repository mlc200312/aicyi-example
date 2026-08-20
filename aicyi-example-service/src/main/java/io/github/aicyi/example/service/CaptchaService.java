package io.github.aicyi.example.service;

import io.github.aicyi.example.domain.SendCaptchaParam;
import io.github.aicyi.example.domain.type.CaptchaType;

import java.awt.image.BufferedImage;

/**
 * @author Mr.Min
 * @description 验证码业务层
 * @date 17:37
 **/
public interface CaptchaService {

    /**
     * 缓存验证码
     *
     * @return
     */
    String saveCaptcha();

    /**
     * 获取验证码
     *
     * @param uuid
     * @return
     */
    BufferedImage getCaptcha(String uuid);

    /**
     * 发送email验证码
     *
     * @param param
     * @return
     */
    String sendEmailCaptcha(SendCaptchaParam param);

    /**
     * 发送短信验证码
     *
     * @param param
     * @return
     */
    String sendSmsCaptcha(SendCaptchaParam param);

    /**
     * 验证验证码
     *
     * @param captchaType
     * @param uuid
     * @param captcha
     */
    void validateCaptcha(CaptchaType captchaType, String uuid, String captcha);
}
