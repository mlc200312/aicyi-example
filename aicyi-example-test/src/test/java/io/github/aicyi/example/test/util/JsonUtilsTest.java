package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.core.codec.JsonCodec;
import io.github.aicyi.commons.util.JsonUtils;
import io.github.aicyi.commons.util.jackson.JacksonJsonCodec;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JsonUtils} 测试类
 */
@DisplayName("JsonUtils 测试")
public class JsonUtilsTest {

    @Getter
    @Setter
    public static class User {
        private Long id;
        private String name;
        private Integer age;

        public User() {
        }

        public User(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

    @Test
    @DisplayName("getInstance 返回全局单例")
    public void testGetInstance() {
        JsonCodec codec = JsonUtils.getInstance();

        assertNotNull(codec);
        assertSame(JacksonJsonCodec.DEFAULT, codec);
    }

    @Test
    @DisplayName("toJson/fromJson 往返一致")
    public void testRoundTrip() {
        JsonCodec codec = JsonUtils.getInstance();
        User user = new User(1001L, "Tom", 18);

        String json = codec.toJson(user);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Tom\""));

        User parsed = codec.fromJson(json, User.class);
        assertNotNull(parsed);
        assertEquals(1001L, parsed.getId());
        assertEquals("Tom", parsed.getName());
        assertEquals(18, parsed.getAge());
    }

    @Test
    @DisplayName("toJson null返回null")
    public void testToJsonNull() {
        assertNull(JsonUtils.getInstance().toJson(null));
    }

    @Test
    @DisplayName("NON_NULL策略不输出null字段")
    public void testNonNullInclusion() {
        JsonCodec codec = JsonUtils.getInstance();
        User user = new User(1001L, null, null);

        String json = codec.toJson(user);

        assertFalse(json.contains("name"));
        assertFalse(json.contains("age"));
    }

    @Test
    @DisplayName("fromJsonList 解析数组")
    public void testFromJsonList() {
        JsonCodec codec = JsonUtils.getInstance();
        List<User> users = Arrays.asList(new User(1L, "a", 10), new User(2L, "b", 20));

        String json = codec.toJson(users);
        List<User> parsed = codec.fromJsonList(json, User.class);

        assertEquals(2, parsed.size());
        assertEquals("a", parsed.get(0).getName());
        assertEquals("b", parsed.get(1).getName());
    }

    @Test
    @DisplayName("fromJsonMap 解析Map")
    public void testFromJsonMap() {
        JsonCodec codec = JsonUtils.getInstance();
        Map<String, User> map = new HashMap<>();
        map.put("u1", new User(1L, "a", 10));

        String json = codec.toJson(map);
        Map<String, User> parsed = codec.fromJsonMap(json, String.class, User.class);

        assertEquals(1, parsed.size());
        assertEquals("a", parsed.get("u1").getName());
    }

    @Test
    @DisplayName("createParameterizedType 泛型反序列化")
    public void testCreateParameterizedType() {
        JsonCodec codec = JsonUtils.getInstance();
        String json = "[{\"id\":1,\"name\":\"a\",\"age\":10}]";

        Type type = codec.createParameterizedType(List.class, User.class);
        List<User> parsed = codec.fromJson(json, type);

        assertEquals(1, parsed.size());
        assertEquals("a", parsed.get(0).getName());
    }

    @Test
    @DisplayName("isEmptyJson 空JSON判断")
    public void testIsEmptyJson() {
        JsonCodec codec = JsonUtils.getInstance();

        assertTrue(codec.isEmptyJson(null));
        assertTrue(codec.isEmptyJson(""));
        assertTrue(codec.isEmptyJson("{}"));
        assertTrue(codec.isEmptyJson("[]"));

        assertFalse(codec.isEmptyJson("{\"a\":1}"));
        assertFalse(codec.isEmptyJson("123"));
    }

    @Test
    @DisplayName("lenient模式容忍未知属性与单引号")
    public void testLenientMode() {
        JsonCodec codec = JsonUtils.getInstance();

        // 未知属性 + 单引号
        User user = codec.fromJson("{'id': 1, 'name': 'a', 'unknownField': 'x'}", User.class);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("a", user.getName());
    }

    @Test
    @DisplayName("非法JSON反序列化抛IllegalArgumentException")
    public void testInvalidJson() {
        JsonCodec codec = JsonUtils.getInstance();

        assertThrows(IllegalArgumentException.class,
                () -> codec.fromJson("{invalid-json", User.class));
    }
}
