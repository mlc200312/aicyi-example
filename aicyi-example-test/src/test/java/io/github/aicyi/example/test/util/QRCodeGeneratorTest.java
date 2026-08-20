package io.github.aicyi.example.test.util;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.github.aicyi.commons.util.CaptchaUtils;
import io.github.aicyi.commons.util.QRCodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link QRCodeGenerator} 测试类
 */
@DisplayName("QRCodeGenerator 二维码生成器测试")
public class QRCodeGeneratorTest {

    @Test
    @DisplayName("generateImage 默认300尺寸")
    public void testGenerateImage() throws Exception {
        BufferedImage image = new QRCodeGenerator("https://aicyi.github.io").generateImage();

        assertNotNull(image);
        assertEquals(300, image.getWidth());
        assertEquals(300, image.getHeight());
    }

    @Test
    @DisplayName("建造者链式配置尺寸与颜色")
    public void testBuilder() throws Exception {
        BufferedImage image = new QRCodeGenerator("aicyi")
                .size(150)
                .frontColor(Color.BLUE)
                .backgroundColor(Color.WHITE)
                .errorCorrection(ErrorCorrectionLevel.H)
                .margin(2)
                .generateImage();

        assertEquals(150, image.getWidth());
        assertEquals(150, image.getHeight());
    }

    @Test
    @DisplayName("generateBytes 输出PNG字节数组")
    public void testGenerateBytes() throws Exception {
        byte[] bytes = new QRCodeGenerator("aicyi").format("PNG").generateBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // PNG魔数
        assertEquals((byte) 0x89, bytes[0]);
    }

    @Test
    @DisplayName("generateToFile 落盘")
    public void testGenerateToFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("qr.png");

        new QRCodeGenerator("aicyi").generateToFile(file.toString());

        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);

        BufferedImage read = ImageIO.read(file.toFile());
        assertEquals(300, read.getWidth());
    }

    @Test
    @DisplayName("generateToStream 写入输出流")
    public void testGenerateToStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new QRCodeGenerator("aicyi").generateToStream(out);

        assertTrue(out.size() > 0);
    }

    @Test
    @DisplayName("带logo生成二维码")
    public void testGenerateWithLogo(@TempDir Path tempDir) throws Exception {
        // 用验证码图片充当logo，写入临时文件
        BufferedImage logo = CaptchaUtils.generateImage("logo");
        Path logoFile = tempDir.resolve("logo.png");
        ImageIO.write(logo, "png", logoFile.toFile());

        BufferedImage image = new QRCodeGenerator("aicyi")
                .size(300)
                .logo(logoFile.toString())
                .generateImage();

        assertNotNull(image);
        assertEquals(300, image.getWidth());
    }

    @Test
    @DisplayName("content为null抛IllegalArgumentException")
    public void testNullContent() {
        assertThrows(IllegalArgumentException.class, () -> new QRCodeGenerator(null));
    }
}
