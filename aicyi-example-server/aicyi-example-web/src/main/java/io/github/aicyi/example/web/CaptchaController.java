package io.github.aicyi.example.web;

import io.github.aicyi.commons.lang.model.Result;
import io.github.aicyi.example.domain.bo.SendCaptchaParam;
import io.github.aicyi.example.service.CaptchaService;
import io.github.aicyi.example.web.dto.SendEmailCaptchaReq;
import io.github.aicyi.example.web.dto.SendSmsCaptchaReq;
import io.github.aicyi.example.web.mapper.CaptchaVoMapper;
import io.github.aicyi.example.web.vo.*;
import io.github.aicyi.midware.web.annotation.IgnoreAuth;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Mr.Min
 * @description 验证码控制器
 * @date 15:13
 **/
@IgnoreAuth
@Tag(name = "验证码控制器")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaVoMapper beanMapper;
    private final CaptchaService captchaService;

    @Operation(summary = "生成验证码", description = "生成验证码")
    @RequestMapping(value = "/get-captcha", method = RequestMethod.GET)
    public Result<GetCaptchaResp> getCaptcha(HttpServletRequest request) {
        String uuid = captchaService.saveCaptcha();
        String captcha = request.getScheme() + "://" + request.getServerName() + "/captcha/" + uuid;
        GetCaptchaResp resp = new GetCaptchaResp();
        resp.setUuid(uuid);
        resp.setCaptcha(captcha);
        return Result.success(resp);
    }

    @Operation(summary = "验证码", description = "验证码")
    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    public void show(HttpServletResponse response, @PathVariable String uuid) {
        BufferedImage image = captchaService.getCaptcha(uuid);

        if (Objects.isNull(image)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            ImageIO.write(image, "jpeg", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "生成邮箱验证码", description = "生成邮箱验证码")
    @RequestMapping(value = "/send-email-captcha", method = RequestMethod.POST)
    public Result<SendEmailCaptchaResp> sendEmailCaptcha(@Validated @RequestBody SendEmailCaptchaReq req) {
        SendCaptchaParam param = beanMapper.toSendCaptchaParam(req);
        String uuid = captchaService.sendEmailCaptcha(param);
        SendEmailCaptchaResp resp = new SendEmailCaptchaResp();
        resp.setUuid(uuid);
        return Result.success(resp);
    }

    @Operation(summary = "生成SMS验证码", description = "生成SMS验证码")
    @RequestMapping(value = "/send-sms-captcha", method = RequestMethod.POST)
    public Result<SendSmsCaptchaResp> sendSmsCaptcha(@Validated @RequestBody SendSmsCaptchaReq req) {
        SendCaptchaParam param = beanMapper.toSendCaptchaParam(req);
        String uuid = captchaService.sendSmsCaptcha(param);
        SendSmsCaptchaResp resp = new SendSmsCaptchaResp();
        resp.setUuid(uuid);
        return Result.success(resp);
    }
}
