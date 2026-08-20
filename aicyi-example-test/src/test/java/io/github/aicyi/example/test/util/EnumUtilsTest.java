package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.lang.EnumType;
import io.github.aicyi.commons.lang.StringEnumType;
import io.github.aicyi.commons.util.EnumUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link EnumUtils} 测试类
 */
@DisplayName("EnumUtils 枚举工具类测试")
public class EnumUtilsTest {

    public enum Gender implements EnumType {
        MAN(1, "男"), WOMAN(2, "女");

        private final int code;
        private final String description;

        Gender(int code, String description) {
            this.code = code;
            this.description = description;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    public enum Season implements StringEnumType {
        SPRING("春", "春季"), SUMMER("夏", "夏季");

        private final String code;
        private final String description;

        Season(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    @Test
    @DisplayName("getType 按整型code查找")
    public void testGetTypeByIntCode() {
        assertEquals(Gender.MAN, EnumUtils.getType(Gender.class, 1));
        assertEquals(Gender.WOMAN, EnumUtils.getType(Gender.class, 2));

        assertNull(EnumUtils.getType(Gender.class, 99));
    }

    @Test
    @DisplayName("getType 按字符串code查找")
    public void testGetTypeByStringCode() {
        assertEquals(Season.SPRING, EnumUtils.getType(Season.class, "春"));
        assertEquals(Season.SUMMER, EnumUtils.getType(Season.class, "夏"));

        assertNull(EnumUtils.getType(Season.class, "秋"));
        assertNull(EnumUtils.getType(Season.class, (String) null));
    }

    @Test
    @DisplayName("equals EnumType比较")
    public void testEqualsEnumType() {
        assertTrue(EnumUtils.equals(Gender.MAN, Gender.MAN));
        assertFalse(EnumUtils.equals(Gender.MAN, Gender.WOMAN));

        assertTrue(EnumUtils.equals((EnumType) null, (EnumType) null));
        assertFalse(EnumUtils.equals(Gender.MAN, null));
        assertFalse(EnumUtils.equals(null, Gender.MAN));
    }

    @Test
    @DisplayName("equals StringEnumType比较")
    public void testEqualsStringEnumType() {
        assertTrue(EnumUtils.equals(Season.SPRING, Season.SPRING));
        assertFalse(EnumUtils.equals(Season.SPRING, Season.SUMMER));

        assertTrue(EnumUtils.equals((StringEnumType) null, (StringEnumType) null));
        assertFalse(EnumUtils.equals(Season.SPRING, null));
    }

    @Test
    @DisplayName("valueOf 通过getter方法匹配值")
    public void testValueOfByMethod() throws NoSuchMethodException {
        Method getCode = Gender.class.getMethod("getCode");

        assertEquals(Gender.MAN, EnumUtils.valueOf(Gender.class, 1, getCode));
        // Number宽化匹配：Long 2 匹配 int code 2
        assertEquals(Gender.WOMAN, EnumUtils.valueOf(Gender.class, 2L, getCode));

        assertNull(EnumUtils.valueOf(Gender.class, 99, getCode));
    }
}
