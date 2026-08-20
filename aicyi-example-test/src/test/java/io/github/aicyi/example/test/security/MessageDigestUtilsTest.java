package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.security.MessageDigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MessageDigestUtils} 单元测试（MD5 / SHA-1 / SHA-256）
 */
@DisplayName("MessageDigestUtils - 消息摘要工具")
class MessageDigestUtilsTest {

    @Test
    @DisplayName("MD5 标准向量（\"abc\" / 空串）")
    void testMd5Vector() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", MessageDigestUtils.generateMd5("abc"));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MessageDigestUtils.generateMd5(""));
    }

    @Test
    @DisplayName("SHA-1 标准向量（\"abc\"）")
    void testSha1Vector() {
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", MessageDigestUtils.generateSha1("abc"));
    }

    @Test
    @DisplayName("SHA-256 标准向量（\"abc\"）")
    void testSha256Vector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                MessageDigestUtils.generateSha256("abc"));
    }

    @Test
    @DisplayName("byte[] 重载与 String 入参结果一致，字符串按 UTF-8 编码")
    void testByteArrayOverloadAndUtf8() {
        String content = "你好，aicyi";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        assertEquals(MessageDigestUtils.generateMd5(bytes), MessageDigestUtils.generateMd5(content));
        assertEquals(MessageDigestUtils.generateSha1(bytes), MessageDigestUtils.generateSha1(content));
        assertEquals(MessageDigestUtils.generateSha256(bytes), MessageDigestUtils.generateSha256(content));
    }
}
