package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.lang.exception.ClockMovedBackwardsException;
import io.github.aicyi.commons.util.id.SnowflakeIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SnowflakeIdGenerator} 测试类
 */
@DisplayName("SnowflakeIdGenerator 雪花ID生成器测试")
public class SnowflakeIdGeneratorTest {

    private static final int WORKER_ID = 7;
    private static final int DATACENTER_ID = 3;

    /**
     * 可控时钟生成器：前 fixedCalls 次调用返回 fixedTime，之后逐次+1
     */
    private static class FixedClockGenerator extends SnowflakeIdGenerator {
        private final long fixedTime;
        private final long fixedCalls;
        private long calls;

        FixedClockGenerator(long workerId, long datacenterId, long epoch,
                            boolean wait, long tolerance, long fixedTime, long fixedCalls) {
            super(workerId, datacenterId, epoch, wait, tolerance);
            this.fixedTime = fixedTime;
            this.fixedCalls = fixedCalls;
        }

        @Override
        protected long currentTimeMillis() {
            calls++;
            return calls <= fixedCalls ? fixedTime : fixedTime + (calls - fixedCalls);
        }
    }

    @Test
    @DisplayName("生成的ID为正数")
    public void testPositiveId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(WORKER_ID, DATACENTER_ID);

        for (int i = 0; i < 100; i++) {
            assertTrue(generator.nextId() > 0);
        }
    }

    @Test
    @DisplayName("单线程生成不重复且单调递增")
    public void testUniqueAndIncreasing() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(WORKER_ID, DATACENTER_ID);
        Set<Long> ids = new HashSet<>();

        long last = -1;
        for (int i = 0; i < 20000; i++) {
            long id = generator.nextId();
            assertTrue(ids.add(id), "出现重复ID: " + id);
            assertTrue(id > last, "ID非单调递增");
            last = id;
        }
    }

    @Test
    @DisplayName("ID编码中包含workerId与datacenterId")
    public void testBitLayout() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(WORKER_ID, DATACENTER_ID);

        long id = generator.nextId();

        assertEquals(WORKER_ID, (id >> 12) & 0x1F);
        assertEquals(DATACENTER_ID, (id >> 17) & 0x1F);
    }

    @Test
    @DisplayName("同毫秒sequence溢出后等待下一毫秒")
    public void testSequenceOverflow() {
        long epoch = SnowflakeIdGenerator.DEFAULT_EPOCH;
        long fixedTime = epoch + 1_000_000L;

        // 前4097次调用停留在同一毫秒，第4097个ID触发sequence回绕并等待下一毫秒
        FixedClockGenerator generator = new FixedClockGenerator(
                0, 0, epoch, true, 5, fixedTime, 4097);

        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 4097; i++) {
            assertTrue(ids.add(generator.nextId()));
        }
        assertEquals(4097, ids.size());

        // 溢出后生成的ID时间位应前进到 fixedTime+1
        long timestamp = ids.stream().mapToLong(Long::longValue).max().orElse(0) >> 22;
        assertEquals(fixedTime + 1 - epoch, timestamp);
    }

    @Test
    @DisplayName("构造参数校验")
    public void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(32, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, 32));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, 0, 0, true, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, 0, System.currentTimeMillis() + 60_000, true, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, 0, SnowflakeIdGenerator.DEFAULT_EPOCH, true, 0));
    }

    @Test
    @DisplayName("时钟回拨不允许等待时立即抛异常")
    public void testClockBackwardsNoWait() {
        long epoch = SnowflakeIdGenerator.DEFAULT_EPOCH;
        long fixedTime = epoch + 1_000_000L;

        // 第2次调用时钟回拨100ms，wait=false立即失败
        FixedClockGenerator generator = new FixedClockGenerator(
                0, 0, epoch, false, 5, fixedTime, 1) {
            private int calls;

            @Override
            protected long currentTimeMillis() {
                calls++;
                return calls == 1 ? fixedTime : fixedTime - 100;
            }
        };

        generator.nextId();
        assertThrows(ClockMovedBackwardsException.class, generator::nextId);
    }

    @Test
    @DisplayName("小回拨在容忍窗口内等待后恢复")
    public void testClockBackwardsSmallWait() {
        long epoch = SnowflakeIdGenerator.DEFAULT_EPOCH;
        long fixedTime = epoch + 1_000_000L;

        // 时钟序列：T -> T-2 -> T（回拨2ms在5ms容忍窗口内，等待后恢复）
        FixedClockGenerator generator = new FixedClockGenerator(
                0, 0, epoch, true, 5, fixedTime, 1) {
            private int calls;

            @Override
            protected long currentTimeMillis() {
                calls++;
                if (calls == 1) {
                    return fixedTime;
                }
                if (calls == 2) {
                    return fixedTime - 2;
                }
                return fixedTime;
            }
        };

        long id1 = generator.nextId();
        long id2 = generator.nextId();

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("大回拨超出容忍窗口抛异常")
    public void testClockBackwardsBeyondTolerance() {
        long epoch = SnowflakeIdGenerator.DEFAULT_EPOCH;
        long fixedTime = epoch + 1_000_000L;

        FixedClockGenerator generator = new FixedClockGenerator(
                0, 0, epoch, true, 5, fixedTime, 1) {
            private int calls;

            @Override
            protected long currentTimeMillis() {
                calls++;
                return calls == 1 ? fixedTime : fixedTime - 100;
            }
        };

        generator.nextId();
        assertThrows(ClockMovedBackwardsException.class, generator::nextId);
    }

    @Test
    @DisplayName("getter返回构造参数")
    public void testGetters() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(WORKER_ID, DATACENTER_ID);

        assertEquals(WORKER_ID, generator.getWorkerId());
        assertEquals(DATACENTER_ID, generator.getDatacenterId());
        assertEquals(SnowflakeIdGenerator.DEFAULT_EPOCH, generator.getEpoch());
    }
}
