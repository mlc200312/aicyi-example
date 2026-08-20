package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.NumberUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NumberUtils} 测试类
 */
@DisplayName("NumberUtils 数字工具类测试")
public class NumberUtilsTest {

    @Test
    @DisplayName("isNull/isNotNull")
    public void testNullCheck() {
        assertTrue(NumberUtils.isNull(null));
        assertFalse(NumberUtils.isNull(0));

        assertTrue(NumberUtils.isNotNull(1));
        assertFalse(NumberUtils.isNotNull(null));
    }

    @Test
    @DisplayName("isPositive 正数判断")
    public void testIsPositive() {
        assertTrue(NumberUtils.isPositive(1));
        assertTrue(NumberUtils.isPositive(0.001d));
        assertTrue(NumberUtils.isPositive(new BigDecimal("0.000001")));

        assertFalse(NumberUtils.isPositive(0));
        assertFalse(NumberUtils.isPositive(-1));
        assertFalse(NumberUtils.isPositive(null));
    }

    @Test
    @DisplayName("isNonNegative 非负判断")
    public void testIsNonNegative() {
        assertTrue(NumberUtils.isNonNegative(0));
        assertTrue(NumberUtils.isNonNegative(1L));

        assertFalse(NumberUtils.isNonNegative(-0.01d));
        assertFalse(NumberUtils.isNonNegative(null));
    }

    @Test
    @DisplayName("isNegative 负数判断")
    public void testIsNegative() {
        assertTrue(NumberUtils.isNegative(-1));
        assertTrue(NumberUtils.isNegative(-0.5f));

        assertFalse(NumberUtils.isNegative(0));
        assertFalse(NumberUtils.isNegative(1));
        assertFalse(NumberUtils.isNegative(null));
    }

    @Test
    @DisplayName("between 区间判断包含边界")
    public void testBetween() {
        assertTrue(NumberUtils.between(5, 1, 10));
        assertTrue(NumberUtils.between(1, 1, 10));
        assertTrue(NumberUtils.between(10, 1, 10));
        assertTrue(NumberUtils.between(5.5d, 1, 10L));

        assertFalse(NumberUtils.between(0, 1, 10));
        assertFalse(NumberUtils.between(11, 1, 10));
    }

    @Test
    @DisplayName("between 任一参数为null返回false")
    public void testBetweenWithNull() {
        assertFalse(NumberUtils.between(null, 1, 10));
        assertFalse(NumberUtils.between(5, null, 10));
        assertFalse(NumberUtils.between(5, 1, null));
    }

    @Test
    @DisplayName("isNumeric 合法数字字符串")
    public void testIsNumeric() {
        assertTrue(NumberUtils.isNumeric("123"));
        assertTrue(NumberUtils.isNumeric("-123.45"));
        assertTrue(NumberUtils.isNumeric(" 123 "));
        assertTrue(NumberUtils.isNumeric("1e10"));

        assertFalse(NumberUtils.isNumeric(null));
        assertFalse(NumberUtils.isNumeric(""));
        assertFalse(NumberUtils.isNumeric("   "));
        assertFalse(NumberUtils.isNumeric("12a"));
    }

    @Test
    @DisplayName("toInt 正常转换与默认值兜底")
    public void testToInt() {
        assertEquals(123, NumberUtils.toInt("123", 0));
        assertEquals(-5, NumberUtils.toInt(" -5 ", 0));

        assertEquals(9, NumberUtils.toInt(null, 9));
        assertEquals(9, NumberUtils.toInt("", 9));
        assertEquals(9, NumberUtils.toInt("abc", 9));
    }
}
