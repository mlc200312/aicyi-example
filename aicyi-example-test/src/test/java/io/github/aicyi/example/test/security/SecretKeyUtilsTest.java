package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.security.SecretKeyUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SecretKeyUtils} 单元测试
 */
@DisplayName("SecretKeyUtils - 密钥转换工具")
class SecretKeyUtilsTest {

    @Test
    @DisplayName("asString 与 toSecretKey 往返转换，密钥字节不丢失")
    void testRoundTrip() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey original = generator.generateKey();

        String base64 = SecretKeyUtils.asString(original);
        SecretKey restored = SecretKeyUtils.toSecretKey(base64, "AES");

        assertNotNull(base64);
        assertEquals("AES", restored.getAlgorithm());
        assertArrayEquals(original.getEncoded(), restored.getEncoded());
    }

    @Test
    @DisplayName("toSecretKeyForHmacSHA256 还原的密钥可直接用于 HmacSHA256 签名")
    void testHmacSha256KeyUsable() throws Exception {
        String base64 = SecretKeyUtils.asString(new javax.crypto.spec.SecretKeySpec(
                "aicyi-hmac-secret-key-0123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                "HmacSHA256"));

        SecretKey key = SecretKeyUtils.toSecretKeyForHmacSHA256(base64);

        assertEquals("HmacSHA256", key.getAlgorithm());

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] sign1 = mac.doFinal("payload".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mac.init(SecretKeyUtils.toSecretKeyForHmacSHA256(base64));
        byte[] sign2 = mac.doFinal("payload".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertTrue(Arrays.equals(sign1, sign2));
        assertEquals(32, sign1.length);
    }
}
