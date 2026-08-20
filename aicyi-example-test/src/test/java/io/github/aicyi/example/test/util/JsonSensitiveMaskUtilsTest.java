package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.JsonSensitiveMaskUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JsonSensitiveMaskUtils} 测试类
 */
@DisplayName("JsonSensitiveMaskUtils 敏感信息脱敏测试")
public class JsonSensitiveMaskUtilsTest {

    private static final String MASK = "******";

    @Test
    @DisplayName("顶层敏感字段脱敏")
    public void testMaskTopLevel() {
        String json = "{\"username\":\"tom\",\"password\":\"123456\",\"age\":18}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertTrue(masked.contains("\"password\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"username\":\"tom\""));
        assertTrue(masked.contains("\"age\":18"));
    }

    @Test
    @DisplayName("嵌套对象敏感字段脱敏")
    public void testMaskNestedObject() {
        String json = "{\"user\":{\"name\":\"tom\",\"accessToken\":\"abc\",\"info\":{\"secret\":\"s\"}}}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertTrue(masked.contains("\"accessToken\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"secret\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"name\":\"tom\""));
    }

    @Test
    @DisplayName("数组内对象敏感字段脱敏")
    public void testMaskArray() {
        String json = "{\"list\":[{\"password\":\"1\"},{\"password\":\"2\"}]}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertFalse(masked.contains("\"password\":\"1\""));
        assertFalse(masked.contains("\"password\":\"2\""));
        assertEquals(2, countOccurrences(masked, "\"" + MASK + "\""));
    }

    @Test
    @DisplayName("敏感字段为数字/布尔/null时统一掩码")
    public void testMaskNonStringValue() {
        String json = "{\"pwd\":123456,\"token\":true,\"authorization\":null}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertTrue(masked.contains("\"pwd\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"token\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"authorization\":\"" + MASK + "\""));
    }

    @Test
    @DisplayName("key匹配忽略大小写且包含匹配")
    public void testKeyMatchCaseInsensitive() {
        String json = "{\"PASSWORD\":\"a\",\"userToken\":\"b\",\"X-Authorization\":\"c\"}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertEquals(3, countOccurrences(masked, "\"" + MASK + "\""));
    }

    @Test
    @DisplayName("非敏感字段值不脱敏")
    public void testNonSensitiveUntouched() {
        String json = "{\"name\":\"tom\",\"remark\":\"my token is here\"}";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        // 值中包含敏感词但key不敏感，不做脱敏
        assertTrue(masked.contains("\"remark\":\"my token is here\""));
    }

    @Test
    @DisplayName("非JSON/空白原样返回")
    public void testNonJsonPassthrough() {
        String notJson = "this is not json {{";

        assertEquals(notJson, JsonSensitiveMaskUtils.maskJsonBody(notJson));
        assertNull(JsonSensitiveMaskUtils.maskJsonBody(null));
        assertEquals("", JsonSensitiveMaskUtils.maskJsonBody(""));
        assertEquals("  ", JsonSensitiveMaskUtils.maskJsonBody("  "));
    }

    @Test
    @DisplayName("数组根节点脱敏")
    public void testArrayRoot() {
        String json = "[{\"password\":\"1\",\"name\":\"a\"}]";

        String masked = JsonSensitiveMaskUtils.maskJsonBody(json);

        assertTrue(masked.contains("\"password\":\"" + MASK + "\""));
        assertTrue(masked.contains("\"name\":\"a\""));
    }

    private static int countOccurrences(String text, String target) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(target, idx)) != -1) {
            count++;
            idx += target.length();
        }
        return count;
    }
}
