package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DateUtils} 测试类（旧 Date API 兼容层）
 */
@DisplayName("DateUtils 时间兼容工具类测试")
public class DateUtilsTest {

    @Test
    @DisplayName("parseDate 默认格式解析")
    public void testParseDateDefault() {
        Date date = DateUtils.parseDate("2026-08-18 10:20:30");

        assertNotNull(date);

        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 30), ldt);
    }

    @Test
    @DisplayName("parseDate 指定格式解析（含纯日期pattern）")
    public void testParseDateWithPattern() {
        // 纯日期pattern：按日期解析并补零点
        Date dateOnly = DateUtils.parseDate("2026-08-18", "yyyy-MM-dd");
        assertNotNull(dateOnly);
        assertEquals(LocalDateTime.of(2026, 8, 18, 0, 0, 0),
                LocalDateTime.ofInstant(dateOnly.toInstant(), ZoneId.systemDefault()));

        // 自定义日期时间pattern
        Date date = DateUtils.parseDate("2026/08/18 10:20", "yyyy/MM/dd HH:mm");
        assertNotNull(date);

        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 0), ldt);
    }

    @Test
    @DisplayName("parseDate null/空白返回null")
    public void testParseDateBlank() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("   "));
    }

    @Test
    @DisplayName("parseDate 非法格式抛IllegalArgumentException")
    public void testParseDateInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> DateUtils.parseDate("not-a-date"));
    }

    @Test
    @DisplayName("formatDate 默认格式输出")
    public void testFormatDateDefault() {
        Date date = Date.from(LocalDateTime.of(2026, 8, 18, 10, 20, 30)
                .atZone(ZoneId.systemDefault()).toInstant());

        assertEquals("2026-08-18 10:20:30", DateUtils.formatDate(date));
    }

    @Test
    @DisplayName("formatDate null返回null")
    public void testFormatDateNull() {
        assertNull(DateUtils.formatDate(null));
        assertNull(DateUtils.formatDate(null, "yyyy-MM-dd"));
    }

    @Test
    @DisplayName("parse与format往返一致")
    public void testParseFormatRoundTrip() {
        String original = "2026-08-18 10:20:30";

        assertEquals(original, DateUtils.formatDate(DateUtils.parseDate(original)));
    }
}
