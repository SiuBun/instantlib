package com.baselib.queue;


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 第二步：直播间信令处理Demo
 * <p>
 * 背景：从队列学习转向实际业务场景
 * 场景：直播间通过WebSocket接收观众行为信令（进场、关注、点赞、送礼等）
 * 挑战：从10人突增到5000人观看，可能产生10000-20000个信令的突发流量
 * <p>
 * 初步方案：使用SynchronousQueue（同事建议）
 * <p>
 * 学习重点：
 * - 实际业务场景中的性能问题
 * - SynchronousQueue在高并发下的表现
 * - 线程池配置对性能的影响
 * - 初步的性能监控
 */
public class Step2_LiveStreamSignalExecutor {
    public static final int SIGN_COUNT = 5000;

    // 使用统一的性能监控器
    // 注意：使用简化模式，适配Step2的直接线程池模式

    /**
     * 通用信令处理器 - 支持传入不同的BlockingQueue
     * 这样就不需要为每种队列类型创建单独的处理器类了
     */
    public static class SignalProcessor {
        private final ThreadPoolExecutor executor;
        private final SignalProcessorMonitor monitor;

        /**
         * 便捷构造函数 - 使用默认线程配置
         */
        public SignalProcessor(BlockingQueue<Runnable> workQueue) {
            int coreThreads = 4;
            int maxThreads = 12;

            this.monitor = new SignalProcessorMonitor();
            this.monitor.setDetailedTracking(false);

            this.executor = new ThreadPoolExecutor(
                    coreThreads,
                    maxThreads,
                    60L, TimeUnit.SECONDS,
                    workQueue,
                    r -> new Thread(r, workQueue.getClass().getSimpleName() + "-Worker"),
                    (r, e) -> {
                        monitor.recordDropped(null);
                        System.out.println("❌ 任务被拒绝：" + workQueue.getClass().getSimpleName() + "队列处理");
                    }
            );
        }

        public void start() {
            monitor.start();
        }

        public void processSignal(Signal signal) {
            try {
                executor.execute(() -> {
                    long latency = handleSignal(signal);
                    monitor.recordProcessed(signal, latency);
                });
            } catch (RejectedExecutionException e) {
                monitor.recordDropped(signal);
                System.out.println("⚠️ 信令提交失败：" + signal.getType().getDesc());
            }
        }

        private long handleSignal(Signal signal) {
            long startTime = System.currentTimeMillis();
            try {
                CommonOperation.handleSignal(signal);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return System.currentTimeMillis() - startTime;
        }

        public void shutdown() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            monitor.printPerformanceReport(executor.getQueue().getClass().getSimpleName() + "处理器");
        }

        public int getActiveThreadCount() {
            return executor.getActiveCount();
        }

        public int getPoolSize() {
            return executor.getPoolSize();
        }

        public int getQueueSize() {
            return executor.getQueue().size();
        }

        public String getQueueType() {
            return executor.getQueue().getClass().getSimpleName();
        }
    }

    /**
     * 通用的信令处理器测试方法
     *
     * @param signalCount 信令数量
     */
    public static void testSignalProcessor(BlockingQueue<Runnable> workQueue, int signalCount) {
        // 创建通用处理器
        SignalProcessor processor = new SignalProcessor(workQueue);
        List<Signal> signals = CommonOperation.generateSignals(signalCount);
        Map<SignalType, Long> distribution = signals.stream()
                .collect(Collectors.groupingBy(Signal::getType, Collectors.counting()));

        System.out.printf(Locale.getDefault(), "信令分布（基于%d个样本）：%n", signalCount);
        distribution.forEach((type, count) ->
                System.out.printf("   %s: %d (%.1f%%)%n", type.getDesc(), count, count / 10.0));
        System.out.println();

        processor.start();

        // 模拟高并发信令接收
        long startTime = System.currentTimeMillis();
        for (Signal signal : signals) {
            processor.processSignal(signal);
        }
        long sendTime = System.currentTimeMillis() - startTime;

        System.out.printf("📨 信令发送完成，耗时：%d ms%n", sendTime);

        // 根据队列类型显示不同的状态信息
        System.out.printf("📊 当前状态：活跃线程=%d，线程池大小=%d，队列大小=%d%n",
                processor.getActiveThreadCount(), processor.getPoolSize(), processor.getQueueSize());

        // 等待处理完成
        try {
            Thread.sleep(10000);  // 等待10秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 显示最终状态
        System.out.printf("📊 最终状态：活跃线程=%d，线程池大小=%d，队列大小=%d%n",
                processor.getActiveThreadCount(), processor.getPoolSize(), processor.getQueueSize());

        processor.shutdown();
    }

//    public static void main(String[] args) throws InterruptedException {
//        System.out.println("🎯 第二步：直播间信令处理Demo");
//        System.out.println("场景：直播间突发流量，10人突增到5000人，产生大量信令");
//        System.out.println("目标：对比SynchronousQueue和ArrayBlockingQueue的性能差异");
//        System.out.println();
//
//        // 打印信令分布
//        List<Signal> testSignals = generateSignals(1000);
//        Map<SignalType, Long> distribution = testSignals.stream()
//                .collect(Collectors.groupingBy(Signal::getType, Collectors.counting()));
//
//        System.out.println("📊 信令分布（基于1000个样本）：");
//        distribution.forEach((type, count) ->
//                System.out.printf("   %s: %d (%d%%)%n", type.getDesc(), count, count/10));
//        System.out.println();
//
//        // 测试SynchronousQueue处理器
//        testSynchronousQueueProcessor();
//
//        System.out.println("\n" + "=".repeat(50) + "\n");
//
//        // 测试ArrayBlockingQueue处理器
//        testArrayBlockingQueueProcessor();
//
//        System.out.println("🎉 第二步测试完成！");
//        System.out.println("\n📚 发现的问题：");
//        System.out.println("1. SynchronousQueue在高并发下线程数暴增");
//        System.out.println("2. 无缓冲导致大量信令被拒绝");
//        System.out.println("3. 线程创建和销毁开销巨大");
//        System.out.println("4. 系统资源消耗过多");
//        System.out.println("\n💡 初步结论：");
//        System.out.println("- SynchronousQueue不适合高并发场景");
//        System.out.println("- ArrayBlockingQueue表现更好，但仍有优化空间");
//        System.out.println("- 需要进一步优化架构和处理策略");
//    }
}