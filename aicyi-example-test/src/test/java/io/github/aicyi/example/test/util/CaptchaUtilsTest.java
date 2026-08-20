package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.CaptchaUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CaptchaUtils} 测试类
 */
@DisplayName("CaptchaUtils 验证码工具类测试")
public class CaptchaUtilsTest {

    /**
     * 与CaptchaUtils.CHAR_SET保持一致（去除了易混淆字符0/O/1/l/i等）
     */
    private static final String CHAR_SET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    @Test
    @DisplayName("randomCaptcha 指定长度且字符在字符集内")
    public void testRandomCaptcha() {
        String captcha = CaptchaUtils.randomCaptcha(6);

        assertEquals(6, captcha.length());
        for (char c : captcha.toCharArray()) {
            assertTrue(CHAR_SET.indexOf(c) >= 0, "非法字符: " + c);
        }
    }

    @Test
    @DisplayName("randomCaptcha 默认长度为4")
    public void testRandomCaptchaDefault() {
        assertEquals(4, CaptchaUtils.randomCaptcha().length());
    }

    @Test
    @DisplayName("randomCaptcha 非法长度抛IllegalArgumentException")
    public void testRandomCaptchaInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.randomCaptcha(0));
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.randomCaptcha(-1));
    }

    @Test
    @DisplayName("generateImage 生成指定尺寸图片")
    public void testGenerateImage() {
        BufferedImage image = CaptchaUtils.generateImage("abcd", 200, 80);

        assertNotNull(image);
        assertEquals(200, image.getWidth());
        assertEquals(80, image.getHeight());
    }

    @Test
    @DisplayName("generateImage 默认尺寸")
    public void testGenerateImageDefault() {
        BufferedImage image = CaptchaUtils.generateImage("abcd");

        assertEquals(150, image.getWidth());
        assertEquals(50, image.getHeight());
    }

    @Test
    @DisplayName("generateImage 非法参数抛IllegalArgumentException")
    public void testGenerateImageInvalid() {
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.generateImage(null));
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.generateImage(""));
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.generateImage("abcd", 0, 50));
        assertThrows(IllegalArgumentException.class, () -> CaptchaUtils.generateImage("abcd", 50, -1));
    }

    @Test
    @DisplayName("toByteArray 输出PNG字节数组")
    public void testToByteArray() throws IOException {
        BufferedImage image = CaptchaUtils.generateImage("abcd");

        byte[] bytes = CaptchaUtils.toByteArray(image, "png");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // PNG魔数
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 0x50, bytes[1]);
    }

    @Test
    @DisplayName("writeToStream 写入输出流")
    public void testWriteToStream() throws IOException {
        BufferedImage image = CaptchaUtils.generateImage("abcd");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        CaptchaUtils.writeToStream(image, out, "png");

        assertTrue(out.size() > 0);
    }
}
