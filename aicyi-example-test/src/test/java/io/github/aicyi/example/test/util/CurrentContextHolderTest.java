package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.CurrentContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CurrentContextHolder} 测试类
 */
@DisplayName("CurrentContextHolder 线程上下文测试")
public class CurrentContextHolderTest {

    @AfterEach
    public void tearDown() {
        // 防止ThreadLocal污染其他用例
        CurrentContextHolder.remove();
    }

    @Test
    @DisplayName("set/get 基本存取")
    public void testSetAndGet() {
        CurrentContextHolder.set("key", "value");

        assertEquals("value", CurrentContextHolder.get("key"));
        assertNull(CurrentContextHolder.get("notExist"));
    }

    @Test
    @DisplayName("未set时get返回null且不产生初始化副作用")
    public void testGetWithoutSet() {
        assertNull(CurrentContextHolder.get("anyKey"));
    }

    @Test
    @DisplayName("remove 清空上下文")
    public void testRemove() {
        CurrentContextHolder.set("key", "value");
        CurrentContextHolder.remove();

        assertNull(CurrentContextHolder.get("key"));
    }

    @Test
    @DisplayName("userId/username 便捷方法")
    public void testUserIdAndUsername() {
        assertNull(CurrentContextHolder.getUserId());
        assertNull(CurrentContextHolder.getUsername());

        CurrentContextHolder.setUserId("1001");
        CurrentContextHolder.setUsername("tom");

        assertEquals("1001", CurrentContextHolder.getUserId());
        assertEquals("tom", CurrentContextHolder.getUsername());

        // 非String值通过toString返回
        CurrentContextHolder.set(CurrentContextHolder.CONTEXT_KEY_USER_ID, 2002L);
        assertEquals("2002", CurrentContextHolder.getUserId());
    }

    @Test
    @DisplayName("线程间隔离")
    public void testThreadIsolation() throws InterruptedException {
        CurrentContextHolder.set("key", "main-thread-value");

        AtomicReference<Object> otherThreadValue = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            otherThreadValue.set(CurrentContextHolder.get("key"));
            latch.countDown();
        });
        thread.start();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertNull(otherThreadValue.get(), "子线程不应读取到主线程上下文");
        assertEquals("main-thread-value", CurrentContextHolder.get("key"));
    }
}
