package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ReflectionUtils} 测试类
 */
@DisplayName("ReflectionUtils 反射工具类测试")
public class ReflectionUtilsTest {

    @Getter
    public static class Parent {
        private final String parentField = "parent";
    }

    @Getter
    public static class Child extends Parent {
        @Setter
        private String name;
        private int age;
        private final Inner inner = new Inner();

        private String secret() {
            return "secret-value";
        }
    }

    @Setter
    @Getter
    public static class Inner {
        private String value;

    }

    public static class GenericDao<T> {
    }

    public static class UserDao extends GenericDao<String> {
    }

    // ========================= getter/setter =========================

    @Test
    @DisplayName("invokeGetter 单级与多级调用")
    public void testInvokeGetter() {
        Child child = new Child();
        child.setName("Tom");
        child.getInner().setValue("deep");

        assertEquals("Tom", ReflectionUtils.invokeGetter(child, "name"));
        assertEquals("deep", ReflectionUtils.invokeGetter(child, "inner.value"));
    }

    @Test
    @DisplayName("invokeSetter 单级与多级调用")
    public void testInvokeSetter() {
        Child child = new Child();

        ReflectionUtils.invokeSetter(child, "name", "Tom");
        assertEquals("Tom", child.getName());

        ReflectionUtils.invokeSetter(child, "inner.value", "deep");
        assertEquals("deep", child.getInner().getValue());
    }

    // ========================= field =========================

    @Test
    @DisplayName("get/setFieldValue 直接访问私有字段（含父类）")
    public void testFieldAccess() {
        Child child = new Child();

        ReflectionUtils.setFieldValue(child, "name", "Tom");
        assertEquals("Tom", ReflectionUtils.getFieldValue(child, "name"));

        // 父类字段
        assertEquals("parent", ReflectionUtils.getFieldValue(child, "parentField"));

        assertThrows(IllegalArgumentException.class,
                () -> ReflectionUtils.getFieldValue(child, "notExist"));
    }

    @Test
    @DisplayName("setFieldValue 数字类型自动转换")
    public void testSetFieldWithNumberConvert() {
        Child child = new Child();

        // Long 写入 int 字段
        ReflectionUtils.setFieldValue(child, "age", 18L);
        assertEquals(18, child.getAge());
    }

    @Test
    @DisplayName("hasField 判断字段存在")
    public void testHasField() {
        Child child = new Child();

        assertTrue(ReflectionUtils.hasField(child, "name"));
        assertTrue(ReflectionUtils.hasField(child, "parentField"));
        assertFalse(ReflectionUtils.hasField(child, "notExist"));
    }

    @Test
    @DisplayName("getAccessibleField 找不到返回null")
    public void testGetAccessibleField() {
        Field field = ReflectionUtils.getAccessibleField(new Child(), "name");
        assertNotNull(field);

        assertNull(ReflectionUtils.getAccessibleField(new Child(), "notExist"));
    }

    // ========================= method =========================

    @Test
    @DisplayName("invokeMethod 按名+参数类型调用")
    public void testInvokeMethod() {
        Child child = new Child();

        Object result = ReflectionUtils.invokeMethod(child, "setName",
                new Class[]{String.class}, new Object[]{"Tom"});

        assertNull(result);
        assertEquals("Tom", child.getName());

        assertThrows(IllegalArgumentException.class,
                () -> ReflectionUtils.invokeMethod(child, "notExist", new Class[]{}, new Object[]{}));
    }

    @Test
    @DisplayName("invokeMethodByName 仅按名调用私有方法")
    public void testInvokeMethodByName() {
        Child child = new Child();

        Object result = ReflectionUtils.invokeMethodByName(child, "secret", new Object[]{});

        assertEquals("secret-value", result);

        assertThrows(IllegalArgumentException.class,
                () -> ReflectionUtils.invokeMethodByName(child, "notExist", new Object[]{}));
    }

    @Test
    @DisplayName("getAccessibleMethod/getAccessibleMethodByName")
    public void testGetAccessibleMethod() {
        Child child = new Child();

        Method method = ReflectionUtils.getAccessibleMethod(child, "setName", String.class);
        assertNotNull(method);

        assertNull(ReflectionUtils.getAccessibleMethod(child, "notExist", String.class));

        assertNotNull(ReflectionUtils.getAccessibleMethodByName(child, "secret"));
        assertNull(ReflectionUtils.getAccessibleMethodByName(child, "notExist"));
    }

    // ========================= generic =========================

    @Test
    @DisplayName("getClassGenericType 获取父类泛型实参")
    public void testGetClassGenericType() {
        assertEquals(String.class, ReflectionUtils.getClassGenericType(UserDao.class));

        // 无泛型父类返回Object.class
        assertEquals(Object.class, ReflectionUtils.getClassGenericType(Child.class));

        // 越界索引返回Object.class
        assertEquals(Object.class, ReflectionUtils.getClassGenericType(UserDao.class, 5));
    }

    // ========================= user class =========================

    @Test
    @DisplayName("getUserClass 普通对象返回自身类型")
    public void testGetUserClass() {
        Child child = new Child();

        assertEquals(Child.class, ReflectionUtils.getUserClass(child));

        assertThrows(IllegalArgumentException.class, () -> ReflectionUtils.getUserClass(null));
    }

    // ========================= convert =========================

    @Test
    @DisplayName("convert 数字与字符串转换")
    public void testConvert() {
        assertEquals(18, ReflectionUtils.convert(18L, int.class));
        assertEquals(18L, ReflectionUtils.convert(18, Long.class));
        assertEquals(1.5f, ReflectionUtils.convert(1.5d, float.class));

        assertEquals("123", ReflectionUtils.convert(123, String.class));
        assertEquals("", ReflectionUtils.convert(null, String.class));

        // 非数字非String原样返回
        Object obj = new Object();
        assertSame(obj, ReflectionUtils.convert(obj, Object.class));
    }

    // ========================= exception convert =========================

    @Test
    @DisplayName("convertReflectionExceptionToUnchecked 各分支")
    public void testConvertException() {
        RuntimeException illegalArgument = ReflectionUtils.convertReflectionExceptionToUnchecked(
                new IllegalArgumentException("test"));
        assertInstanceOf(IllegalArgumentException.class, illegalArgument);

        RuntimeException runtime = new IllegalStateException("rt");
        assertSame(runtime, ReflectionUtils.convertReflectionExceptionToUnchecked(runtime));

        RuntimeException unexpected = ReflectionUtils.convertReflectionExceptionToUnchecked(
                new Exception("checked"));
        assertEquals("Unexpected Checked Exception.", unexpected.getMessage());
    }

    // 防止未使用import告警的占位断言
    @Test
    @DisplayName("泛型Dao类型体系校验")
    public void testGenericHierarchy() {
        assertTrue(List.class.isInterface());
        assertEquals(String.class, ReflectionUtils.getClassGenericType(UserDao.class, 0));
    }
}
