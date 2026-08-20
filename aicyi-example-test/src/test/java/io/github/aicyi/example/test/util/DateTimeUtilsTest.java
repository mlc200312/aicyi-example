package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.DateTimeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DateTimeUtils} 测试类
 */
@DisplayName("DateTimeUtils 时间工具类测试")
public class DateTimeUtilsTest {

    // ========================= now =========================

    @Test
    @DisplayName("now系列方法与系统时钟一致")
    public void testNowMethods() {
        long before = System.currentTimeMillis();
        long nowMillis = DateTimeUtils.nowMillis();
        long after = System.currentTimeMillis();

        assertTrue(nowMillis >= before && nowMillis <= after);

        // 秒级换算允许两次取时跨秒的1秒偏差
        long seconds = DateTimeUtils.nowSeconds();
        assertTrue(Math.abs(seconds - nowMillis / 1000) <= 1, "nowSeconds偏差");
        assertNotNull(DateTimeUtils.now());
        assertNotNull(DateTimeUtils.today());
        assertNotNull(DateTimeUtils.nowDateTime());
        assertNotNull(DateTimeUtils.zone());
    }

    // ========================= convert =========================

    @Test
    @DisplayName("LocalDateTime与Instant互转往返")
    public void testConvertRoundTrip() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 18, 10, 20, 30);

        Instant instant = DateTimeUtils.toInstant(time);
        LocalDateTime back = DateTimeUtils.toLocalDateTime(instant);

        assertEquals(time, back);
        assertEquals(instant.toEpochMilli(), DateTimeUtils.toEpochMilli(time));
    }

    // ========================= parse =========================

    @Test
    @DisplayName("parseDate/parseDateTime 指定格式解析")
    public void testParseWithPattern() {
        assertEquals(LocalDate.of(2026, 8, 18),
                DateTimeUtils.parseDate("2026-08-18", DateTimeUtils.DATE_PATTERN));

        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 30),
                DateTimeUtils.parseDateTime("2026-08-18 10:20:30", DateTimeUtils.DATE_TIME_PATTERN));

        // 支持前后空白trim
        assertEquals(LocalDate.of(2026, 8, 18),
                DateTimeUtils.parseDate("  2026-08-18  ", DateTimeUtils.DATE_PATTERN));

        assertThrows(DateTimeParseException.class,
                () -> DateTimeUtils.parseDate("not-a-date", DateTimeUtils.DATE_PATTERN));
    }

    @Test
    @DisplayName("parseAuto 自动识别epoch毫秒")
    public void testParseAutoEpochMillis() {
        LocalDateTime expected = Instant.ofEpochMilli(1755483630000L)
                .atZone(DateTimeUtils.zone()).toLocalDateTime();

        assertEquals(expected, DateTimeUtils.parseAuto("1755483630000"));
    }

    @Test
    @DisplayName("parseAuto 自动识别epoch秒")
    public void testParseAutoEpochSecond() {
        LocalDateTime expected = Instant.ofEpochSecond(1755483630L)
                .atZone(DateTimeUtils.zone()).toLocalDateTime();

        assertEquals(expected, DateTimeUtils.parseAuto("1755483630"));
    }

    @Test
    @DisplayName("parseAuto 自动识别常见日期时间格式")
    public void testParseAutoPatterns() {
        assertEquals(LocalDateTime.of(2026, 8, 18, 0, 0, 0),
                DateTimeUtils.parseAuto("2026-08-18"));

        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 30),
                DateTimeUtils.parseAuto("2026-08-18 10:20:30"));

        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 30, 123_000_000),
                DateTimeUtils.parseAuto("2026-08-18 10:20:30.123"));
    }

    @Test
    @DisplayName("parseAuto 非法输入返回null")
    public void testParseAutoInvalid() {
        assertNull(DateTimeUtils.parseAuto(null));
        assertNull(DateTimeUtils.parseAuto(""));
        assertNull(DateTimeUtils.parseAuto("not-a-date"));
        assertNull(DateTimeUtils.parseAuto("2026-13-45"));
    }

    // ========================= format =========================

    @Test
    @DisplayName("format 指定格式输出")
    public void testFormat() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 18, 10, 20, 30);

        assertEquals("2026-08-18", DateTimeUtils.format(time, DateTimeUtils.DATE_PATTERN));
        assertEquals("2026-08-18 10:20:30", DateTimeUtils.format(time, DateTimeUtils.DATE_TIME_PATTERN));

        assertThrows(IllegalArgumentException.class,
                () -> DateTimeUtils.format(null, DateTimeUtils.DATE_PATTERN));
    }

    @Test
    @DisplayName("formatNow 输出当前时间")
    public void testFormatNow() {
        String formatted = DateTimeUtils.formatNow(DateTimeUtils.DATE_TIME_PATTERN);

        assertNotNull(formatted);
        assertEquals(19, formatted.length());
    }

    // ========================= compare =========================

    @Test
    @DisplayName("isBetween 区间判断含边界")
    public void testIsBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 31, 23, 59, 59);

        assertTrue(DateTimeUtils.isBetween(LocalDateTime.of(2026, 8, 18, 12, 0, 0), start, end));
        assertTrue(DateTimeUtils.isBetween(start, start, end));
        assertTrue(DateTimeUtils.isBetween(end, start, end));

        assertFalse(DateTimeUtils.isBetween(LocalDateTime.of(2026, 7, 31, 23, 59, 59), start, end));
    }

    @Test
    @DisplayName("isToday 判断是否今天")
    public void testIsToday() {
        assertTrue(DateTimeUtils.isToday(LocalDateTime.now()));
        assertFalse(DateTimeUtils.isToday(LocalDateTime.of(2000, 1, 1, 0, 0, 0)));
    }

    // ========================= calculate =========================

    @Test
    @DisplayName("betweenDays/betweenSeconds 差值计算")
    public void testBetween() {
        assertEquals(17, DateTimeUtils.betweenDays(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 18)));
        assertEquals(-17, DateTimeUtils.betweenDays(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 1)));

        assertEquals(90, DateTimeUtils.betweenSeconds(
                LocalDateTime.of(2026, 8, 18, 10, 0, 0),
                LocalDateTime.of(2026, 8, 18, 10, 1, 30)));
    }

    // ========================= truncate =========================

    @Test
    @DisplayName("truncateToMinute/startOfDay/endOfDay")
    public void testTruncateAndBoundary() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 18, 10, 20, 30, 123_000_000);

        assertEquals(LocalDateTime.of(2026, 8, 18, 10, 20, 0),
                DateTimeUtils.truncateToMinute(time));

        LocalDate date = LocalDate.of(2026, 8, 18);
        assertEquals(LocalDateTime.of(2026, 8, 18, 0, 0, 0), DateTimeUtils.startOfDay(date));
        // endOfDay的末位999为纳秒（LocalTime.of的nanoOfSecond语义）
        assertEquals(LocalDateTime.of(2026, 8, 18, 23, 59, 59, 999),
                DateTimeUtils.endOfDay(date));
    }
}
