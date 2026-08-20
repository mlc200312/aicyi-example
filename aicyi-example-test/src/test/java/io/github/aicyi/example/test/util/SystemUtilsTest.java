package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.SystemUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SystemUtils} 测试类
 */
@DisplayName("SystemUtils 系统环境工具类测试")
public class SystemUtilsTest {

    @Test
    @DisplayName("getIpAddress 返回非null结果")
    public void testGetIpAddressNotNull() {
        String ip = SystemUtils.getIpAddress();

        // 解析失败时兜底返回空串，不允许返回null
        assertNotNull(ip);
    }

    @Test
    @DisplayName("getIpAddress 成功时为IPv4格式")
    public void testGetIpAddressFormat() {
        String ip = SystemUtils.getIpAddress();

        if (!ip.isEmpty()) {
            assertTrue(ip.matches("\\d{1,3}(\\.\\d{1,3}){3}"), "非法IPv4格式: " + ip);
        }
    }
}
