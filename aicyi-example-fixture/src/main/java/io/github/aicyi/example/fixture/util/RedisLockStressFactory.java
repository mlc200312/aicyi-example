package io.github.aicyi.example.fixture.util;

import io.github.aicyi.commons.core.lock.DistributedLock;
import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Redisson DistributedLock 压测工厂
 * <p>
 * 支持：
 * - 高并发同时起跑
 * - 多次循环压测
 * - TPS统计
 * - 成功失败统计
 * - waitTime统计
 * - holdTime统计
 */
public class RedisLockStressFactory extends ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockStressFactory.class);

    private final String factoryName;
    private final int workerCount;
    private final int loopPerWorker;
    private final Robot robot;

    private final AtomicInteger productCount = new AtomicInteger();

    private final LongAdder successCount = new LongAdder();
    private final LongAdder failedCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();

    private final LongAdder totalWaitNanos = new LongAdder();
    private final LongAdder totalHoldNanos = new LongAdder();

    private final List<Long> waitSamples = new CopyOnWriteArrayList<>();

    private volatile long endTime;

    public RedisLockStressFactory(
            int workerCount,
            int loopPerWorker,
            String factoryName,
            Robot robot
    ) {
        super(workerCount);
        this.factoryName = factoryName;
        this.workerCount = workerCount;
        this.loopPerWorker = loopPerWorker;
        this.robot = robot;
    }

    public RedisLockStressFactory(
            int workerCount,
            int loopPerWorker,
            Robot robot
    ) {
        this(workerCount, loopPerWorker, "RedissonStressFactory", robot);
    }

    /**
     * 开始压测
     */
    public void startRun() throws InterruptedException {

        LOGGER.info("========== {} 开始压测 ==========", factoryName);
        LOGGER.info("线程数: {}", workerCount);
        LOGGER.info("每线程循环次数: {}", loopPerWorker);
        LOGGER.info("总请求数: {}", workerCount * loopPerWorker);

        CountDownLatch readyGate = new CountDownLatch(workerCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(workerCount);

        for (int i = 0; i < workerCount; i++) {
            String workerNo = String.format("%06d", i);

            executor.submit(new Worker(
                    workerNo,
                    readyGate,
                    startGate,
                    endGate
            ));
        }

        // 等待全部线程就绪
        readyGate.await();

        LOGGER.info("全部线程已就绪，开始同时起跑");

        long realStart = System.nanoTime();

        startGate.countDown();

        endGate.await();

        endTime = System.nanoTime();

        printReport(realStart, endTime);

        shutdown();
    }

    private void shutdown() {
        executor.shutdown();
    }

    private void printReport(long start, long end) {

        long costNs = end - start;
        double costSec = costNs / 1_000_000_000D;

        long totalRequests = (long) workerCount * loopPerWorker;
        long success = successCount.sum();
        long failed = failedCount.sum();
        long errors = errorCount.sum();

        double tps = success / costSec;

        double avgWaitMs = totalWaitNanos.sum() / 1_000_000D / Math.max(1, success);

        double avgHoldMs = totalHoldNanos.sum() / 1_000_000D / Math.max(1, success);

        LOGGER.info("========== 压测报告 ==========");
        LOGGER.info("总请求数: {}", totalRequests);
        LOGGER.info("成功数: {}", success);
        LOGGER.info("失败数: {}", failed);
        LOGGER.info("异常数: {}", errors);
        LOGGER.info("生产数: {}", productCount.get());

        LOGGER.info("总耗时(s): {}", String.format("%.3f", costSec));
        LOGGER.info("TPS: {}", String.format("%.2f", tps));

        LOGGER.info("平均等待锁(ms): {}", String.format("%.3f", avgWaitMs));
        LOGGER.info("平均持锁(ms): {}", String.format("%.3f", avgHoldMs));

        if (!waitSamples.isEmpty()) {
            List<Long> sorted = new ArrayList<>(waitSamples);
            sorted.sort(Long::compareTo);

            LOGGER.info("P95等待(ms): {}", nanosToMs(percentile(sorted, 95)));

            LOGGER.info("P99等待(ms): {}", nanosToMs(percentile(sorted, 99)));
        }

        LOGGER.info("============================");
    }

    private long percentile(List<Long> sorted, int p) {
        int index = (int) Math.ceil(sorted.size() * p / 100.0) - 1;
        return sorted.get(Math.max(index, 0));
    }

    private String nanosToMs(long nanos) {
        return String.format("%.3f", nanos / 1_000_000D);
    }

    private class Worker implements Runnable {

        private final String workerNo;
        private final CountDownLatch readyGate;
        private final CountDownLatch startGate;
        private final CountDownLatch endGate;

        public Worker(
                String workerNo,
                CountDownLatch readyGate,
                CountDownLatch startGate,
                CountDownLatch endGate
        ) {
            this.workerNo = workerNo;
            this.readyGate = readyGate;
            this.startGate = startGate;
            this.endGate = endGate;
        }

        @Override
        public void run() {
            try {
                readyGate.countDown();

                startGate.await();

                for (int i = 0; i < loopPerWorker; i++) {
                    doWork(i);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endGate.countDown();
            }
        }

        private void doWork(int round) {

            DistributedLock lock = robot.getLock();

            long waitStart = System.nanoTime();

            boolean locked = false;

            try {
                locked = lock.tryLock(Duration.ofSeconds(10));

                long waitCost = System.nanoTime() - waitStart;

                waitSamples.add(waitCost);

                if (!locked) {
                    failedCount.increment();
                    return;
                }

                successCount.increment();
                totalWaitNanos.add(waitCost);

                long holdStart = System.nanoTime();

                String result = robot.working();

                int productNo = productCount.incrementAndGet();

                LOGGER.info(
                        "[{}-{}] success => {} product={}",
                        workerNo,
                        round,
                        result,
                        productNo
                );

                long holdCost = System.nanoTime() - holdStart;
                totalHoldNanos.add(holdCost);

            } catch (Exception e) {
                errorCount.increment();

                LOGGER.error(
                        "[{}-{}] error",
                        workerNo,
                        round,
                        e
                );

            } finally {
                if (locked) {
                    try {
                        lock.unlock();
                    } catch (Exception e) {
                        LOGGER.warn("unlock failed", e);
                    }
                }
            }
        }
    }

    /**
     * 工厂器械
     */
    public interface Robot {

        /**
         * 获取锁（建议 RedissonLock）
         */
        DistributedLock getLock();

        /**
         * 模拟业务
         */
        String working();
    }
}