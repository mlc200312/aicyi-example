package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.lang.model.Pair;
import io.github.aicyi.commons.security.RsaUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RsaUtils} 单元测试（RSA-2048 加解密 / SHA256WithRSA 签名验签）
 */
@DisplayName("RsaUtils - RSA 非对称加解密与签名")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RsaUtilsTest {

    /**
     * 2048 位密钥对生成较慢，整个测试类共享一份密钥对
     */
    private Pair<byte[], byte[]> keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @BeforeAll
    void setUp() {
        keyPair = RsaUtils.generateKeyPair();
        publicKey = RsaUtils.restorePublicKey(keyPair.getKey());
        privateKey = RsaUtils.restorePrivateKey(keyPair.getValue());
    }

    @Test
    @DisplayName("generateKeyPair 返回非空编码，可还原为 RSA 密钥且字节一致")
    void testGenerateAndRestoreKeyPair() {
        assertNotNull(keyPair.getKey());
        assertNotNull(keyPair.getValue());
        assertTrue(keyPair.getKey().length > 0);
        assertTrue(keyPair.getValue().length > 0);

        assertEquals("RSA", publicKey.getAlgorithm());
        assertEquals("RSA", privateKey.getAlgorithm());
        assertArrayEquals(keyPair.getKey(), publicKey.getEncoded());
        assertArrayEquals(keyPair.getValue(), privateKey.getEncoded());
    }

    @Test
    @DisplayName("公钥加密、私钥解密往返一致，支持中文内容")
    void testEncryptDecryptRoundTrip() {
        String content = "rsa secret，你好世界";

        String cipher = RsaUtils.encrypt(content, publicKey);

        assertNotNull(cipher);
        assertEquals(content, RsaUtils.decrypt(cipher, privateKey));
    }

    @Test
    @DisplayName("篡改密文后解密抛 IllegalStateException")
    void testDecryptTamperedCipher() {
        String cipher = RsaUtils.encrypt("tamper-me", publicKey);
        String tampered = cipher.substring(0, cipher.length() - 4) + "AAAA";

        assertThrows(IllegalStateException.class, () -> RsaUtils.decrypt(tampered, privateKey));
    }

    @Test
    @DisplayName("私钥签名、公钥验签：内容被篡改后验签失败")
    void testSignAndVerify() {
        String content = "sign-me-please";

        String sign = RsaUtils.sign(content, privateKey);

        assertNotNull(sign);
        assertTrue(RsaUtils.verify(content, sign, publicKey));
        assertFalse(RsaUtils.verify(content + "-tampered", sign, publicKey));
    }

    @Test
    @DisplayName("文件签名与验签：文件内容被篡改后验签失败")
    void testSignFileAndVerifyFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("data.txt").toFile();
        Files.write(file.toPath(), "file-content-for-sign".getBytes(StandardCharsets.UTF_8));

        String sign = RsaUtils.signFile(file, privateKey);

        assertNotNull(sign);
        assertTrue(RsaUtils.verifyFile(file, sign.getBytes(StandardCharsets.UTF_8), publicKey));

        Files.write(file.toPath(), "file-content-tampered".getBytes(StandardCharsets.UTF_8));
        assertFalse(RsaUtils.verifyFile(file, sign.getBytes(StandardCharsets.UTF_8), publicKey));
    }

    @Test
    @DisplayName("非法密钥字节还原抛 IllegalStateException")
    void testRestoreInvalidKeyBytes() {
        byte[] garbage = "not-a-valid-key".getBytes(StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class, () -> RsaUtils.restorePublicKey(garbage));
        assertThrows(IllegalStateException.class, () -> RsaUtils.restorePrivateKey(garbage));
    }
}
