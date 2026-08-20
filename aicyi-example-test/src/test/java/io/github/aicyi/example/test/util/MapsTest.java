package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.Maps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Maps} 测试类
 */
@DisplayName("Maps Map构造器测试")
public class MapsTest {

    @Test
    @DisplayName("of 构建单键Map")
    public void testOfSingleEntry() {
        Map<String, Integer> map = Maps.of("age", 18).build();

        assertEquals(1, map.size());
        assertEquals(18, map.get("age"));
    }

    @Test
    @DisplayName("and 链式追加多个键值对")
    public void testAndChain() {
        Map<String, Object> map = Maps.<String, Object>of("name", "Tom")
                .and("age", 18)
                .and("vip", true)
                .build();

        assertEquals(3, map.size());
        assertEquals("Tom", map.get("name"));
        assertEquals(18, map.get("age"));
        assertEquals(true, map.get("vip"));
    }

    @Test
    @DisplayName("重复key以最后一次为准")
    public void testDuplicateKey() {
        Map<String, Integer> map = Maps.of("k", 1).and("k", 2).build();

        assertEquals(1, map.size());
        assertEquals(2, map.get("k"));
    }

    @Test
    @DisplayName("ofStr 构建String键Map")
    public void testOfStr() {
        Map<String, Object> map = Maps.ofStr("key", 123).build();

        assertEquals(123, map.get("key"));
    }

    @Test
    @DisplayName("build 返回不可修改Map")
    public void testBuildUnmodifiable() {
        Map<String, Integer> map = Maps.of("k", 1).build();

        assertThrows(UnsupportedOperationException.class, () -> map.put("new", 2));
    }

    @Test
    @DisplayName("允许null值")
    public void testNullValue() {
        Map<String, Object> map = Maps.of("k", null).build();

        assertTrue(map.containsKey("k"));
        assertNull(map.get("k"));
    }
}
