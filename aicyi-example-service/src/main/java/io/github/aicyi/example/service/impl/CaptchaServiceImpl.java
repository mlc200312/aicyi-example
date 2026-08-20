package io.github.aicyi.example.service.impl;

import io.github.aicyi.commons.core.cache.Cache;
import io.github.aicyi.commons.lang.model.BaseBean;
import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.exception.BusinessException;
import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.commons.util.CaptchaUtils;
import io.github.aicyi.commons.util.UUIDUtils;
import io.github.aicyi.example.domain.SendCaptchaParam;
import io.github.aicyi.example.service.util.Constants;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.domain.type.CaptchaType;
import io.github.aicyi.example.domain.type.ExampleResultCode;
import io.github.aicyi.example.service.CaptchaService;
import io.github.aicyi.example.service.UserService;
import io.github.aicyi.midware.utils.SpringEnvironmentHelper;
import io.github.aicyi.commons.core.message.MessageContent;
import io.github.aicyi.commons.core.message.MessageSendCallback;
import io.github.aicyi.commons.core.message.MessageSendResult;
import io.github.aicyi.midware.message.core.sender.UnifiedMessageManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:38
 **/
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UnifiedMessageManager unifiedMessageManager;
    private final Cache<String, String> stringCache;
    private final UserService userService;

    @Override
    public String saveCaptcha() {
        // 生成验证码
        String captcha = CaptchaUtils.randomCaptcha();
        String uuid = UUIDUtils.generateV7Id();

        // 缓存验证码
        String captchaKey = Constants.getCaptchaKey(uuid);
        stringCache.put(captchaKey, captcha);
        return uuid;
    }

    @Override
    public BufferedImage getCaptcha(String uuid) {
        String code = stringCache.get(Constants.getCaptchaKey(uuid));
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return CaptchaUtils.generateImage(code);
    }

    @Override
    public String sendEmailCaptcha(SendCaptchaParam param) {
        return sendCaptcha(param, messageContentParam ->
                Constants.getEmailMessageContent(messageContentParam.getCaptchaType(), messageContentParam.getCaptcha(), messageContentParam.getEmail()));
    }

    @Override
    public String sendSmsCaptcha(SendCaptchaParam param) {
        return sendCaptcha(param, messageContentParam ->
                Constants.getSmsMessageContent(messageContentParam.getCaptchaType(), messageContentParam.getCaptcha(), messageContentParam.getMobile()));
    }

    @Override
    public void validateCaptcha(CaptchaType captchaType, String uuid, String captcha) {
        // 验证码缓存key
        String captchaKey = Constants.getCaptchaKey(captchaType, uuid);
        // 获取缓存验证码
        String code = stringCache.get(captchaKey);
        boolean isEq = captcha.equalsIgnoreCase(code);
        // 测试环境不验证
        if (!SpringEnvironmentHelper.isProd()) {
            logger.info("验证码校验：{}", isEq);
            return;
        }
        if (!isEq) {
            stringCache.evict(Constants.getCaptchaKey(uuid));
            throw new BusinessException("验证码错误");
        }
    }

    private String sendCaptcha(SendCaptchaParam param, Function<MessageContentParam, MessageContent<?>> function) {
        // 验证码校验
        validateCaptcha(CaptchaType.IMAGE_CAPTCHA_TYPE, param.getUuid(), param.getVerCode());

        // 查询用户信息
        User user = userService.getByUsername(param.getUsername());

        if (Objects.isNull(user)) {
            throw new BusinessException(ExampleResultCode.OBJECT_NOT_FOUND);
        }

        // 生成验证码
        CaptchaType captchaType = param.getCaptchaType();
        String captcha = CaptchaUtils.randomCaptcha();
        String uuid = UUIDUtils.generateV7Id();

        MessageContentParam messageContentParam = new MessageContentParam();
        messageContentParam.setCaptchaType(captchaType);
        messageContentParam.setCaptcha(captcha);
        messageContentParam.setEmail(user.getEmail());
        messageContentParam.setMobile(user.getMobile());

        MessageContent<?> messageContent = function.apply(messageContentParam);

        unifiedMessageManager.sendAsync(messageContent, new MessageSendCallback() {
            @Override
            public void onComplete(MessageSendResult result) {

                // 缓存验证码
                String captchaKey = Constants.getCaptchaKey(captchaType, uuid);

                stringCache.put(captchaKey, captcha);
            }

            @Override
            public void onError(Exception e) {
                logger.error(e, "验证码消息发送失败");
            }
        });
        return uuid;
    }


    @Getter
    @Setter
    static class MessageContentParam extends BaseBean implements BoBean {
        private CaptchaType captchaType;
        private String captcha;
        private String mobile;
        private String email;
    }
}
