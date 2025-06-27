package com.baselib.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 通用信令处理器性能监控器
 * <p>
 * 提供完整的信令处理生命周期监控：
 * 1. 接收阶段：接收总数、成功入队、接收丢弃
 * 2. 消费阶段：从接收队列取出
 * 3. 执行阶段：提交给线程池、线程池拒绝
 * 4. 处理阶段：处理完成、处理失败
 * 5. 延迟统计：总延迟、最大延迟、最小延迟、平均延迟
 * 6. 分类统计：按信令类型统计处理和丢弃
 * 7. 性能指标：吞吐量、丢失率、处理成功率
 * <p>
 * 支持多种报告模式：
 * - 详细生命周期报告（适用于 Producer-Consumer 模式）
 * - 简化性能报告（适用于直接线程池模式）
 * - 兼容旧版本API
 */
public class SignalProcessorMonitor {

    /** === 生命周期统计 === */
    /**
     * 接收总数
     */
    private final AtomicLong received = new AtomicLong(0);
    /**
     * 成功入receiveQueue
     */
    private final AtomicLong enqueuedToReceive = new AtomicLong(0);
    /**
     * receiveQueue满丢弃
     */
    private final AtomicLong droppedAtReceive = new AtomicLong(0);
    /**
     * 从receiveQueue取出
     */
    private final AtomicLong dequeued = new AtomicLong(0);
    /**
     * 提交给executor
     */
    private final AtomicLong submittedToExecutor = new AtomicLong(0);
    /**
     * executor拒绝
     */
    private final AtomicLong droppedAtExecutor = new AtomicLong(0);
    /**
     * 处理完成
     */
    private final AtomicLong processed = new AtomicLong(0);
    /**
     * 处理失败
     */
    private final AtomicLong failed = new AtomicLong(0);

    /** === 简化统计（兼容ComprehensivePerformanceMonitor） === */
    /**
     * 总丢弃数（简化模式）
     */
    private final AtomicLong totalDropped = new AtomicLong(0);

    /** === 延迟统计 === */
    /**
     * 总延迟时间
     */
    private final AtomicLong totalLatency = new AtomicLong(0);
    /**
     * 最大延迟时间
     */
    private final AtomicLong maxLatency = new AtomicLong(0);
    /**
     * 最小延迟时间
     */
    private final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);

    /** === 分类统计 === */
    /**
     * 按类型统计处理数量
     */
    private final Map<SignalType, AtomicLong> processedByType = new HashMap<>();
    /**
     * 按类型统计丢弃数量
     */
    private final Map<SignalType, AtomicLong> droppedByType = new HashMap<>();
    /**
     * 按类型的延迟统计
     */
    private final Map<SignalType, AtomicLong> latencyByType = new HashMap<>();
    /**
     * 按类型的处理次数
     */
    private final Map<SignalType, AtomicLong> countByType = new HashMap<>();

    /** === 系统状态 === */
    /**
     * 开始时间
     */
    private volatile long startTime;
    /**
     * 结束时间
     */
    private volatile long endTime;
    /**
     * 最大线程数
     */
    private volatile int maxThreads = 0;
    /**
     * 是否使用详细跟踪模式
     */
    private volatile boolean useDetailedTracking = true;

    public SignalProcessorMonitor() {
        // 初始化分类统计
        for (SignalType type : SignalType.values()) {
            processedByType.put(type, new AtomicLong(0));
            droppedByType.put(type, new AtomicLong(0));
            latencyByType.put(type, new AtomicLong(0));
            countByType.put(type, new AtomicLong(0));
        }
    }

    // === 配置方法 ===
    public void setDetailedTracking(boolean enabled) {
        this.useDetailedTracking = enabled;
    }

    // === 启动和结束 ===
    public void start() {
        startTime = System.currentTimeMillis();
        endTime = 0;
    }

    public void stop() {
        endTime = System.currentTimeMillis();
    }

    public long getDuration() {
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    // === 生命周期记录方法 ===
    public void recordReceived() {
        received.incrementAndGet();
    }

    public void recordEnqueuedToReceive() {
        enqueuedToReceive.incrementAndGet();
    }

    public void recordDroppedAtReceive() {
        droppedAtReceive.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
    }

    public void recordDroppedAtReceive(Signal signal) {
        droppedAtReceive.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
        if (signal != null) {
            droppedByType.get(signal.getType()).incrementAndGet();
        }
    }

    public void recordDequeued() {
        dequeued.incrementAndGet();
    }

    public void recordSubmittedToExecutor() {
        submittedToExecutor.incrementAndGet();
    }

    public void recordDroppedAtExecutor() {
        droppedAtExecutor.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
    }

    public void recordDroppedAtExecutor(Signal signal) {
        droppedAtExecutor.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
        if (signal != null) {
            droppedByType.get(signal.getType()).incrementAndGet();
        }
    }

    public void recordFailed() {
        failed.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
    }

    public void recordFailed(Signal signal) {
        failed.incrementAndGet();
        if (!useDetailedTracking) {
            totalDropped.incrementAndGet();
        }
        if (signal != null) {
            droppedByType.get(signal.getType()).incrementAndGet();
        }
    }

    public void recordProcessed(long latency) {
        processed.incrementAndGet();
        totalLatency.addAndGet(latency);
        maxLatency.updateAndGet(current -> Math.max(current, latency));
        minLatency.updateAndGet(current -> Math.min(current, latency));
    }

    public void recordProcessed(Signal signal, long latency) {
        processed.incrementAndGet();
        totalLatency.addAndGet(latency);
        maxLatency.updateAndGet(current -> Math.max(current, latency));
        minLatency.updateAndGet(current -> Math.min(current, latency));

        if (signal != null) {
            processedByType.get(signal.getType()).incrementAndGet();
            latencyByType.get(signal.getType()).addAndGet(latency);
            countByType.get(signal.getType()).incrementAndGet();
        }
    }

    // === 兼容方法（ComprehensivePerformanceMonitor风格） ===
    public void recordDropped(Signal signal) {
        // 兼容方法：在简化模式下直接记录总丢弃
        if (useDetailedTracking) {
            recordDroppedAtReceive(signal);
        } else {
            totalDropped.incrementAndGet();
            if (signal != null) {
                droppedByType.get(signal.getType()).incrementAndGet();
            }
        }
    }

    public void updateMaxThreads(int threads) {
        maxThreads = Math.max(maxThreads, threads);
    }

    // === 获取统计数据方法 ===
    public long getReceived() {
        return received.get();
    }

    public long getEnqueuedToReceive() {
        return enqueuedToReceive.get();
    }

    public long getDroppedAtReceive() {
        return droppedAtReceive.get();
    }

    public long getDequeued() {
        return dequeued.get();
    }

    public long getSubmittedToExecutor() {
        return submittedToExecutor.get();
    }

    public long getDroppedAtExecutor() {
        return droppedAtExecutor.get();
    }

    public long getProcessed() {
        return processed.get();
    }

    public long getFailed() {
        return failed.get();
    }

    public long getTotalDropped() {
        if (useDetailedTracking) {
            return droppedAtReceive.get() + droppedAtExecutor.get() + failed.get();
        } else {
            return totalDropped.get();
        }
    }

    public long getTotal() {
        if (useDetailedTracking) {
            return received.get();
        } else {
            return processed.get() + getTotalDropped();
        }
    }

    public double getThroughput() {
        long duration = getDuration();
        return duration > 0 ? processed.get() / (duration / 1000.0) : 0;
    }

    public double getDropRate() {
        long total = getTotal();
        return total > 0 ? (double) getTotalDropped() / total * 100 : 0;
    }

    public double getSuccessRate() {
        long total = getTotal();
        return total > 0 ? (double) processed.get() / total * 100 : 0;
    }

    public long getAverageLatency() {
        return processed.get() > 0 ? totalLatency.get() / processed.get() : 0;
    }

    // === 详细报告打印（适用于Producer-Consumer模式和直接线程池模式） ===
    public void printDetailedReport(long receiveQueueSize, long executorQueueSize, long activeThreads) {
        System.out.printf("\n=== 完整统计报告 ===%n");
        System.out.printf("接收信令总数: %d%n", received.get());
        System.out.printf("成功入receiveQueue: %d%n", enqueuedToReceive.get());
        System.out.printf("receiveQueue满丢弃: %d%n", droppedAtReceive.get());
        System.out.printf("从receiveQueue取出: %d%n", dequeued.get());
        System.out.printf("提交给executor: %d%n", submittedToExecutor.get());
        System.out.printf("executor拒绝: %d%n", droppedAtExecutor.get());
        System.out.printf("处理完成: %d%n", processed.get());
        System.out.printf("处理失败: %d%n", failed.get());

        System.out.println("--- 当前状态 ---");
        System.out.printf("receiveQueue剩余: %d%n", receiveQueueSize);
        System.out.printf("executor队列剩余: %d%n", executorQueueSize);
        System.out.printf("正在处理线程数: %d%n", activeThreads);

        System.out.println("--- 流向分析 ---");
        System.out.printf("各种丢弃: %d%n", getTotalDropped());

        // ✅ 正确的统计验证
        long totalProcessedOrDropped = processed.get() + failed.get() + droppedAtReceive.get() + droppedAtExecutor.get();
        long totalInProgress = receiveQueueSize + executorQueueSize + activeThreads;
        long calculatedTotal = totalProcessedOrDropped + totalInProgress;

        System.out.println("--- 统计验证 ---");
        System.out.printf("已完成处理: %d%n", processed.get() + failed.get());
        System.out.printf("各种丢弃: %d%n", droppedAtReceive.get() + droppedAtExecutor.get());
        System.out.printf("还在处理中: %d%n", totalInProgress);
        System.out.printf("统计总计: %d (应该等于接收总数%d)%n", calculatedTotal, received.get());

        // ✅ 关键指标验证
        System.out.println("--- 关键指标验证 ---");
        long receiveBalance = enqueuedToReceive.get() + droppedAtReceive.get();
        System.out.printf("入队 + 丢弃 = %d + %d = %d (应该等于接收总数%d)%n",
                enqueuedToReceive.get(), droppedAtReceive.get(), receiveBalance, received.get());

        long executorBalance = submittedToExecutor.get() + droppedAtExecutor.get();
        System.out.printf("提交 + 拒绝 = %d + %d = %d (应该等于取出数%d)%n",
                submittedToExecutor.get(), droppedAtExecutor.get(), executorBalance, dequeued.get());
        System.out.println("-".repeat(60));
    }

    // === 性能报告打印（Step4风格 - 直接线程池模式） ===
    public void printPerformanceReport(String processorName) {
        long duration = getDuration();
        long total = getTotal();
        double throughput = getThroughput();
        double dropRate = getDropRate();
        long avgLatency = getAverageLatency();

        System.out.printf("\n=== %s 性能报告 ===%n", processorName);
        System.out.printf("总耗时: %d ms%n", duration);
        System.out.printf("总信令: %d 个%n", total);
        System.out.printf("处理成功: %d 个%n", processed.get());
        System.out.printf("丢弃信令: %d 个%n", getTotalDropped());
        System.out.printf("吞吐量: %.1f 信令/秒%n", throughput);
        System.out.printf("丢失率: %.1f%%%n", dropRate);
        System.out.printf("成功率: %.1f%%%n", getSuccessRate());
        System.out.printf("平均延迟: %d ms%n", avgLatency);
        System.out.printf("最大延迟: %d ms%n", maxLatency.get());
        System.out.printf("最小延迟: %d ms%n", minLatency.get() == Long.MAX_VALUE ? 0 : minLatency.get());
        System.out.printf("最大线程数: %d%n", maxThreads);

        // 分类统计
        System.out.println("\n📊 分类统计:");
        for (SignalType type : SignalType.values()) {
            long typeProcessed = processedByType.get(type).get();
            long typeDropped = droppedByType.get(type).get();
            long typeTotal = typeProcessed + typeDropped;
            double typeDropRate = typeTotal > 0 ? (double) typeDropped / typeTotal * 100 : 0;
            long typeCount = countByType.get(type).get();
            long typeAvgLatency = typeCount > 0 ? latencyByType.get(type).get() / typeCount : 0;

            if (typeTotal > 0) {  // 只显示有数据的类型
                System.out.printf("   %s: 处理=%d, 丢弃=%d, 丢失率=%.1f%%, 平均延迟=%dms%n",
                        type.getDesc(), typeProcessed, typeDropped, typeDropRate, typeAvgLatency);
            }
        }
        System.out.println("==============================\n");
    }

    // === 简化报告（兼容ComprehensivePerformanceMonitor） ===
    public void printDetailedReport(String processorName) {
        printPerformanceReport(processorName);
    }

    // === 实时状态报告 ===
    public void printRealTimeStatus(String processorName) {
        long duration = getDuration();
        System.out.printf("\n[%s] 实时状态 (运行时间: %d ms)%n", processorName, duration);
        System.out.printf("处理中: %d, 已完成: %d, 已丢弃: %d, 吞吐量: %.1f/s%n",
                submittedToExecutor.get() - processed.get() - failed.get(),
                processed.get(), getTotalDropped(), getThroughput());
    }

    // === 重置方法 ===
    public void reset() {
        received.set(0);
        enqueuedToReceive.set(0);
        droppedAtReceive.set(0);
        dequeued.set(0);
        submittedToExecutor.set(0);
        droppedAtExecutor.set(0);
        processed.set(0);
        failed.set(0);
        totalDropped.set(0);
        totalLatency.set(0);
        maxLatency.set(0);
        minLatency.set(Long.MAX_VALUE);
        maxThreads = 0;

        for (SignalType type : SignalType.values()) {
            processedByType.get(type).set(0);
            droppedByType.get(type).set(0);
            latencyByType.get(type).set(0);
            countByType.get(type).set(0);
        }

        startTime = 0;
        endTime = 0;
    }
} 