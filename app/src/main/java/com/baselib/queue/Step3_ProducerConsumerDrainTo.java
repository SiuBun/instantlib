package com.baselib.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 第三步：生产者消费者模式优化
 * <p>
 * 背景：发现SynchronousQueue问题后，探索更好的架构
 * 新思路：分离信令接收和处理，采用经典的Producer-Consumer模式
 * 友商方案：信令接收层offer入队，独立消费者线程take数据，使用drainTo批量处理
 * <p>
 * 核心改进：
 * 1. 分离接收队列和处理线程池 - 避免直接耦合
 * 2. 使用take()阻塞获取 - 避免CPU空转
 * 3. 引入批量处理 - 提升吞吐量
 * 4. 优先级排序 - 保证重要信令优先处理
 * <p>
 * 学习重点：
 * - Producer-Consumer架构设计
 * - take()vs poll()的性能差异
 * - drainTo批量获取的优势
 * - 优先级处理策略
 */
public class Step3_ProducerConsumerDrainTo {

    public static final int SIGN_COUNT = 5000;
    public static final int RECEIVE_QUEUE_CAP = 500;
    public static final int EXECUTOR_QUEUE_CAP = 200;


    /**
     * 消费者循环策略接口
     */
    @FunctionalInterface
    interface ConsumerStrategy {
        void consume(BlockingQueue<Signal> receiveQueue, ThreadPoolExecutor executor,
                     SignalProcessorMonitor monitor, ProducerConsumerProcessor processor);
    }

    /**
     * 统一的Producer-Consumer处理器
     * 特点：通过策略模式支持不同的消费者循环逻辑
     */
    static class ProducerConsumerProcessor {
        private final BlockingQueue<Signal> receiveQueue;      // 接收队列
        private final ThreadPoolExecutor executor;             // 处理线程池
        private final Thread consumerThread;                   // 消费者线程
        private final SignalProcessorMonitor monitor;
        private final String processorName;                    // 处理器名称
        private volatile boolean running = true;

        public ProducerConsumerProcessor(ConsumerStrategy consumerStrategy, String processorName, String threadPrefix) {
            this.processorName = processorName;
            this.monitor = new SignalProcessorMonitor();

            // 1. 创建接收队列 - 使用LinkedBlockingQueue应对突发流量
            this.receiveQueue = new LinkedBlockingQueue<>(RECEIVE_QUEUE_CAP);

            // 2. 创建处理线程池 - 使用ArrayBlockingQueue保证低延迟
            this.executor = new ThreadPoolExecutor(
                    4,                                      // 核心线程数
                    12,                                     // 最大线程数
                    60L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(EXECUTOR_QUEUE_CAP),          // 处理队列
                    r -> new Thread(r, threadPrefix + "-Worker"),
                    (r, e) -> {
                        // ✅ 每个被拒绝的任务代表一个信令被丢弃
                        monitor.recordDroppedAtExecutor();
                    }
            );

            // 3. 创建消费者线程 - 使用传入的策略
            this.consumerThread = new Thread(() -> {
                consumerStrategy.consume(receiveQueue, executor, monitor, this);
            }, threadPrefix + "-Consumer");
        }

        public void start() {
            monitor.start();
            consumerThread.start();
            System.out.println("✅ " + processorName + "处理器启动");
        }

        // 生产者方法：WebSocket回调调用
        public void receiveSignal(Signal signal) {
            monitor.recordReceived();  // 统计接收总数

            if (!receiveQueue.offer(signal)) {
                // 对于高优先级信令，尝试阻塞入队
                if (signal.getType().getPriority() >= 2) {
                    try {
                        receiveQueue.put(signal);  // 阻塞直到能入队
                        monitor.recordEnqueuedToReceive();  // 统计成功入队
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        monitor.recordDroppedAtReceive();  // 中断也算丢弃
                    }
                } else {
                    monitor.recordDroppedAtReceive();  // 低优先级直接丢弃
                }
            } else {
                monitor.recordEnqueuedToReceive();  // 统计成功入队
            }
        }

        // 提供给策略使用的方法
        public boolean isRunning() {
            return running;
        }

        // 提供给策略使用的信令处理方法
        public void processSignal(Signal signal) {
            try {
                CommonOperation.handleSignal(signal);

                // 记录处理成功和延迟
                long latency = System.currentTimeMillis() - signal.getCreateTime();
                monitor.recordProcessed(latency);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                monitor.recordFailed();  // 统计处理失败
            } catch (Exception e) {
                monitor.recordFailed();  // 统计其他异常
            }
        }

        public void shutdown() {
            running = false;
            consumerThread.interrupt();

            try {
                consumerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            long receiveQueueSize = receiveQueue.size();
            long executorQueueSize = executor.getQueue().size();
            long activeThreads = executor.getActiveCount();

            System.out.printf("\n=== 完整统计报告 ===%n");
            monitor.printDetailedReport(receiveQueueSize, executorQueueSize, activeThreads);
        }
    }

    /**
     * 基础消费者策略：逐个处理信令
     */
    static class BasicConsumerStrategy implements ConsumerStrategy {
        @Override
        public void consume(BlockingQueue<Signal> receiveQueue, ThreadPoolExecutor executor,
                            SignalProcessorMonitor monitor, ProducerConsumerProcessor processor) {
            while (processor.isRunning()) {
                try {
                    // 使用take()阻塞等待 - 避免CPU空转
                    Signal signal = receiveQueue.take();
                    monitor.recordDequeued();  // ✅ 统计从receiveQueue取出

                    // 提交到线程池处理 - 统计由拒绝策略和任务本身处理
                    monitor.updateMaxThreads(executor.getPoolSize());
                    executor.execute(() -> {
                        monitor.recordSubmittedToExecutor();  // ✅ 在任务内部统计成功提交
                        processor.processSignal(signal);
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 批量消费者策略：使用drainTo批量获取并处理
     */
    static class BatchConsumerStrategy implements ConsumerStrategy {
        private static final int BATCH_SIZE = 10;              // 批处理大小

        @Override
        public void consume(BlockingQueue<Signal> receiveQueue, ThreadPoolExecutor executor,
                            SignalProcessorMonitor monitor, ProducerConsumerProcessor processor) {
            List<Signal> batch = new ArrayList<>(BATCH_SIZE);

            while (processor.isRunning()) {
                try {
                    Signal firstSignal = receiveQueue.take();
                    monitor.recordDequeued();  // 统计从receiveQueue取出
                    batch.add(firstSignal);

                    int drained = receiveQueue.drainTo(batch, BATCH_SIZE - 1);
                    for (int i = 0; i < drained; i++) {
                        monitor.recordDequeued();  // 统计批量取出的每一个
                    }

                    // 按优先级排序（高优先级在前）
                    batch.sort((s1, s2) -> Integer.compare(s2.getType().getPriority(), s1.getType().getPriority()));

                    final List<Signal> currentBatch = new ArrayList<>(batch);
                    batch.clear();

                    monitor.updateMaxThreads(executor.getPoolSize());

                    // ✅ 为了解决批量统计问题，我们改为逐个提交
                    // 这样拒绝策略就能正确统计每个被拒绝的信令
                    for (Signal signal : currentBatch) {
                        executor.execute(() -> {
                            monitor.recordSubmittedToExecutor();
                            processor.processSignal(signal);
                        });
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 测试基础Producer-Consumer处理器
     */
    public void testProducerConsumer() {
        ProducerConsumerProcessor[] processors = {
                new ProducerConsumerProcessor(
                        new BasicConsumerStrategy(),
                        "BasicProducerConsumer",
                        "Processor"
                ),
                new ProducerConsumerProcessor(
                        new BatchConsumerStrategy(),
                        "BatchProducerConsumer",
                        "Processor"
                )};
        List<Signal> signals = CommonOperation.generateSignals(SIGN_COUNT);

        System.out.printf("📦 生成测试信令: %d 个%n", signals.size());

        // 打印信令分布
        System.out.println("📊 信令分布:");
        Map<SignalType, Long> distribution = signals.stream()
                .collect(Collectors.groupingBy(Signal::getType, Collectors.counting()));
        distribution.forEach((type, count) ->
                System.out.printf("   %s: %d (%.1f%%)%n", type.getDesc(), count, count * 100.0 / Step3_ProducerConsumerDrainTo.SIGN_COUNT));
        System.out.println();

        for (ProducerConsumerProcessor processor : processors) {
            processor.start();

            // 模拟高并发接收
            long startTime = System.currentTimeMillis();
            for (Signal signal : signals) {
                processor.receiveSignal(signal);
            }
            long sendTime = System.currentTimeMillis() - startTime;

            System.out.printf("📨 信令发送完成，耗时：%d ms%n", sendTime);

            // 等待处理完成
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            processor.shutdown();
        }
    }

//    public static void main(String[] args) {
//        System.out.println("🎯 第三步：Producer-Consumer模式优化");
//        System.out.println("背景：分离信令接收和处理，引入友商的批量处理方案");
//        System.out.println("目标：对比基础方案和批量处理方案的性能差异");
//        System.out.println();
//
//        // 测试基础Producer-Consumer
//        testBasicProducerConsumer();
//
//        System.out.println("\n" + "=".repeat(60) + "\n");
//
//        // 测试批量Producer-Consumer
//        testBatchProducerConsumer();
//
//        System.out.println("🎉 第三步测试完成！");
//        System.out.println("\n📚 关键发现：");
//        System.out.println("1. Producer-Consumer架构成功分离了接收和处理");
//        System.out.println("2. take()阻塞获取避免了CPU空转");
//        System.out.println("3. drainTo批量获取显著提升了吞吐量");
//        System.out.println("4. 优先级排序保证了重要信令优先处理");
//        System.out.println("5. 批量处理方案明显优于逐个处理");
//
//        System.out.println("\n💡 优化方向：");
//        System.out.println("- 进一步优化队列选择和配置");
//        System.out.println("- 完善智能背压控制策略");
//        System.out.println("- 增加多消费者线程提升并发度");
//        System.out.println("- 完善性能监控和指标");
//    }
}