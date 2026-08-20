package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.security.AesUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AesUtils} 单元测试（AES/GCM/NoPadding）
 */
@DisplayName("AesUtils - AES/GCM 加解密工具")
class AesUtilsTest {

    private static final String KEY_16 = "1234567890abcdef";
    private static final String KEY_24 = "1234567890abcdef12345678";
    private static final String KEY_32 = "1234567890abcdef1234567890abcdef";

    @Test
    @DisplayName("16/24/32 字节密钥均可加解密往返，支持中文内容")
    void testEncryptDecryptRoundTrip() {
        String content = "hello aes，你好世界 123！@#";

        assertEquals(content, AesUtils.aesDecrypt(AesUtils.aesEncrypt(content, KEY_16), KEY_16));
        assertEquals(content, AesUtils.aesDecrypt(AesUtils.aesEncrypt(content, KEY_24), KEY_24));
        assertEquals(content, AesUtils.aesDecrypt(AesUtils.aesEncrypt(content, KEY_32), KEY_32));
    }

    @Test
    @DisplayName("每次加密使用随机 IV：相同明文密文不同，且均可解密")
    void testRandomIv() {
        String content = "same-content";

        String cipher1 = AesUtils.aesEncrypt(content, KEY_16);
        String cipher2 = AesUtils.aesEncrypt(content, KEY_16);

        assertNotEquals(cipher1, cipher2);
        assertEquals(content, AesUtils.aesDecrypt(cipher1, KEY_16));
        assertEquals(content, AesUtils.aesDecrypt(cipher2, KEY_16));
    }

    @Test
    @DisplayName("密文结构：Base64(12 字节 IV || 密文 || 16 字节 GCM 认证标签)")
    void testCipherTextStructure() {
        String content = "structure-check";

        byte[] decoded = Base64.decodeBase64(AesUtils.aesEncrypt(content, KEY_16));

        // IV(12) + 明文长度 + TAG(16)
        assertEquals(12 + content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + 16, decoded.length);
    }

    @Test
    @DisplayName("空白入参原样返回（null / 空串 / 纯空白）")
    void testBlankInputPassThrough() {
        assertNull(AesUtils.aesEncrypt(null, KEY_16));
        assertEquals("", AesUtils.aesEncrypt("", KEY_16));
        assertEquals("   ", AesUtils.aesEncrypt("   ", KEY_16));

        assertNull(AesUtils.aesDecrypt(null, KEY_16));
        assertEquals("", AesUtils.aesDecrypt("", KEY_16));
        assertEquals("   ", AesUtils.aesDecrypt("   ", KEY_16));
    }

    @Test
    @DisplayName("使用错误密钥解密抛 IllegalStateException（GCM 认证失败）")
    void testDecryptWithWrongKey() {
        String cipher = AesUtils.aesEncrypt("secret-data", KEY_16);

        assertThrows(IllegalStateException.class, () -> AesUtils.aesDecrypt(cipher, KEY_32));
    }

    @Test
    @DisplayName("密钥长度非 16/24/32 字节时抛 IllegalArgumentException")
    void testInvalidKeyLength() {
        assertThrows(IllegalArgumentException.class, () -> AesUtils.aesEncrypt("data", "short-key"));
        assertThrows(IllegalArgumentException.class, () -> AesUtils.aesDecrypt("data", "short-key"));
        assertThrows(IllegalArgumentException.class, () -> AesUtils.aesEncrypt("data", null));
    }

    @Test
    @DisplayName("篡改密文后解密抛 IllegalStateException（完整性校验生效）")
    void testTamperedCipherText() {
        byte[] decoded = Base64.decodeBase64(AesUtils.aesEncrypt("tamper-me", KEY_16));
        decoded[decoded.length - 1] ^= 0x01;

        assertThrows(IllegalStateException.class,
                () -> AesUtils.aesDecrypt(Base64.encodeBase64String(decoded), KEY_16));
    }

    @Test
    @DisplayName("密文长度不足（<= 12 字节）时抛 IllegalArgumentException")
    void testTooShortCipherText() {
        assertThrows(IllegalArgumentException.class,
                () -> AesUtils.aesDecrypt(Base64.encodeBase64String(new byte[12]), KEY_16));
    }
}
