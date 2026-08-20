package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.UUIDUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UUIDUtils} 测试类
 */
@DisplayName("UUIDUtils 测试")
public class UUIDUtilsTest {

    @Test
    @DisplayName("生成32位无横线UUID")
    public void testGenerateFormat() {
        String id = UUIDUtils.generateV7Id();

        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
        assertTrue(id.matches("[0-9a-f]{32}"));
    }

    @Test
    @DisplayName("批量生成不重复")
    public void testGenerateUniqueness() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            assertTrue(ids.add(UUIDUtils.generateV7Id()), "第" + i + "次生成出现重复");
        }

        assertEquals(10000, ids.size());
    }
}
