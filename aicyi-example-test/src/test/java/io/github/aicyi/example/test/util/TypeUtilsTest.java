package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.TypeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TypeUtils} 测试类
 */
@DisplayName("TypeUtils 类型工具类测试")
public class TypeUtilsTest {

    @Test
    @DisplayName("isPrimitive 基本类型判断")
    public void testIsPrimitive() {
        assertTrue(TypeUtils.isPrimitive(int.class));
        assertTrue(TypeUtils.isPrimitive(void.class));

        assertFalse(TypeUtils.isPrimitive(Integer.class));
        assertFalse(TypeUtils.isPrimitive(String.class));

        assertThrows(IllegalArgumentException.class, () -> TypeUtils.isPrimitive(null));
    }

    @Test
    @DisplayName("isWrapperType 包装类型判断")
    public void testIsWrapperType() {
        assertTrue(TypeUtils.isWrapperType(Integer.class));
        assertTrue(TypeUtils.isWrapperType(Boolean.class));
        assertTrue(TypeUtils.isWrapperType(Void.class));

        assertFalse(TypeUtils.isWrapperType(int.class));
        assertFalse(TypeUtils.isWrapperType(String.class));

        assertThrows(IllegalArgumentException.class, () -> TypeUtils.isWrapperType(null));
    }

    @Test
    @DisplayName("isPrimitiveOrWrapper 基本或包装类型")
    public void testIsPrimitiveOrWrapper() {
        assertTrue(TypeUtils.isPrimitiveOrWrapper(long.class));
        assertTrue(TypeUtils.isPrimitiveOrWrapper(Long.class));

        assertFalse(TypeUtils.isPrimitiveOrWrapper(Object.class));
    }

    @Test
    @DisplayName("wrapPrimitiveType 基本类型装箱")
    public void testWrapPrimitiveType() {
        assertEquals(Integer.class, TypeUtils.wrapPrimitiveType(int.class));
        assertEquals(Long.class, TypeUtils.wrapPrimitiveType(long.class));
        assertEquals(Boolean.class, TypeUtils.wrapPrimitiveType(boolean.class));
        assertEquals(Character.class, TypeUtils.wrapPrimitiveType(char.class));
        assertEquals(Float.class, TypeUtils.wrapPrimitiveType(float.class));
        assertEquals(Double.class, TypeUtils.wrapPrimitiveType(double.class));
        assertEquals(Byte.class, TypeUtils.wrapPrimitiveType(byte.class));
        assertEquals(Short.class, TypeUtils.wrapPrimitiveType(short.class));
        assertEquals(Void.class, TypeUtils.wrapPrimitiveType(void.class));

        // 非基本类型原样返回
        assertEquals(String.class, TypeUtils.wrapPrimitiveType(String.class));
    }

    @Test
    @DisplayName("unwrapWrapperType 包装类型拆箱")
    public void testUnwrapWrapperType() {
        assertEquals(int.class, TypeUtils.unwrapWrapperType(Integer.class));
        assertEquals(long.class, TypeUtils.unwrapWrapperType(Long.class));
        assertEquals(boolean.class, TypeUtils.unwrapWrapperType(Boolean.class));
        assertEquals(char.class, TypeUtils.unwrapWrapperType(Character.class));
        assertEquals(float.class, TypeUtils.unwrapWrapperType(Float.class));
        assertEquals(double.class, TypeUtils.unwrapWrapperType(Double.class));
        assertEquals(byte.class, TypeUtils.unwrapWrapperType(Byte.class));
        assertEquals(short.class, TypeUtils.unwrapWrapperType(Short.class));
        assertEquals(void.class, TypeUtils.unwrapWrapperType(Void.class));

        // 非包装类型原样返回
        assertEquals(String.class, TypeUtils.unwrapWrapperType(String.class));
    }

    @Test
    @DisplayName("wrap与unwrap互为逆运算")
    public void testWrapUnwrapRoundTrip() {
        Class<?>[] primitives = {int.class, long.class, boolean.class, char.class,
                float.class, double.class, byte.class, short.class};

        for (Class<?> primitive : primitives) {
            assertEquals(primitive, TypeUtils.unwrapWrapperType(TypeUtils.wrapPrimitiveType(primitive)));
        }
    }
}
