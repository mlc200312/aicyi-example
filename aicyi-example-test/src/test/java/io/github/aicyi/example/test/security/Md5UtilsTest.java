package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.security.Md5Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Md5Utils} 单元测试
 */
@DisplayName("Md5Utils - MD5 摘要工具")
class Md5UtilsTest {

    @Test
    @DisplayName("标准向量：md5(\"abc\")")
    void testKnownVector() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", Md5Utils.md5("abc"));
    }

    @Test
    @DisplayName("标准向量：md5(\"\") 与稳定性")
    void testEmptyAndStable() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5Utils.md5(""));
        assertEquals(Md5Utils.md5("aicyi"), Md5Utils.md5("aicyi"));
    }
}
