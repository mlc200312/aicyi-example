package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.security.token.jwt.JWTInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link JWTInfo} 单元测试
 */
@DisplayName("JWTInfo - JWT 信息模型")
class JWTInfoTest {

    @Test
    @DisplayName("三参构造器：id / uniqueName / deviceId 均可读取")
    void testThreeArgsConstructor() {
        JWTInfo info = new JWTInfo("1001", "tester", "device-01");

        assertEquals("1001", info.getId());
        assertEquals("tester", info.getUniqueName());
        assertEquals("device-01", info.getDeviceId());
    }

    @Test
    @DisplayName("无参构造器与 setter 可正常读写")
    void testNoArgConstructorAndSetters() {
        JWTInfo info = new JWTInfo();

        assertNull(info.getId());

        info.setId("2002");
        info.setUniqueName("setter-user");
        info.setDeviceId("device-02");

        assertEquals("2002", info.getId());
        assertEquals("setter-user", info.getUniqueName());
        assertEquals("device-02", info.getDeviceId());
    }

    @Test
    @DisplayName("equals：字段全等返回 true；id / uniqueName / deviceId 任一不同返回 false")
    void testEquals() {
        JWTInfo base = new JWTInfo("1001", "tester", "device-01");

        assertEquals(base, new JWTInfo("1001", "tester", "device-01"));
        assertEquals(base.hashCode(), new JWTInfo("1001", "tester", "device-01").hashCode());

        assertNotEquals(base, new JWTInfo("9999", "tester", "device-01"));
        assertNotEquals(base, new JWTInfo("1001", "other", "device-01"));
        // BaseBean.reflectionEquals 参与比较全部字段，deviceId 不同同样不相等
        assertNotEquals(base, new JWTInfo("1001", "tester", "device-02"));
        assertNotEquals(base, null);
    }
}
