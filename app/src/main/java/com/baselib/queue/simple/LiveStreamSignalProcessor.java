package com.baselib.queue.simple;

import com.baselib.queue.entity.Signal;
import com.baselib.queue.entity.SignalProcessorMonitor;

import java.util.concurrent.*;
import java.util.*;
import java.util.Locale;

/**
 * 直播间信令处理器 - 最终完善方案
 * 
 * 核心特性：
 * 1. LinkedBlockingQueue接收队列 - 应对突发流量
 * 2. ArrayBlockingQueue线程池队列 - 保证低延迟和内存可控
 * 3. take()阻塞获取 - 避免CPU空转
 * 4. 批量处理 - 提升吞吐量
 * 5. 优先级排序 - 保证重要信令优先处理
 * 6. 智能背压控制 - 根据信令优先级决定丢弃策略
 * 7. 性能监控 - 实时监控处理效果
 * 
 * 适用场景：直播间10人突增到5000人，产生10000-20000个信令的高并发场景
 * 
 * @author 基于多轮性能测试优化的最终方案
 */
public class LiveStreamSignalProcessor {
    // 使用统一的性能监控器，支持完整的生命周期跟踪
    public static final int SIGN_COUNT = 5000;
    // 配置参数
    private static final int RECEIVE_QUEUE_SIZE = 500;      // 接收队列大小
    private static final int THREAD_POOL_QUEUE_SIZE = 400;  // 线程池队列大小
    private static final int CORE_THREADS = 4;              // 核心线程数
    private static final int MAX_THREADS = 12;              // 最大线程数
    private static final int CONSUMER_THREADS = 3;          // 消费者线程数
    private static final int BATCH_SIZE = 50;               // 批处理大小
    
    // 核心组件
    private final BlockingQueue<Signal> receiveQueue;       // LinkedBlockingQueue接收队列
    private final ThreadPoolExecutor executor;              // 信令处理线程池
    private final Thread[] consumers;                       // 消费者线程数组
    private final SignalProcessorMonitor monitor;          // 统一性能监控器
    private volatile boolean running = true;                // 运行状态标识
    
    /**
     * 构造函数 - 初始化所有组件
     */
    public LiveStreamSignalProcessor() {
        // 1. 创建接收队列 - LinkedBlockingQueue，适合高并发生产
        this.receiveQueue = new LinkedBlockingQueue<>(RECEIVE_QUEUE_SIZE);
        
        // 2. 创建线程池 - ArrayBlockingQueue作为任务队列，保证低延迟和内存可控
        this.executor = new ThreadPoolExecutor(
            CORE_THREADS,                           // 核心线程数
            MAX_THREADS,                            // 最大线程数
            60L, TimeUnit.SECONDS,                  // 空闲线程存活时间
            new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE),  // ArrayBlockingQueue任务队列
            r -> {
                Thread t = new Thread(r, "SignalProcessor-Worker");
                t.setDaemon(false);  // 非守护线程，确保任务完成
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：调用者运行
        );
        
        // 3. 创建性能监控器，使用详细跟踪模式
        this.monitor = new SignalProcessorMonitor();
        this.monitor.setDetailedTracking(true);  // 使用详细模式，完整跟踪生命周期
        
        // 4. 创建并启动消费者线程
        this.consumers = new Thread[CONSUMER_THREADS];
        for (int i = 0; i < CONSUMER_THREADS; i++) {
            consumers[i] = new Thread(this::consumerLoop, "SignalConsumer-" + i);
            consumers[i].start();
        }
        
        // 5. 启动性能监控
        monitor.start();
        
        System.out.println("✅ 直播间信令处理器初始化完成");
        System.out.printf("   接收队列: LinkedBlockingQueue(%d)%n", RECEIVE_QUEUE_SIZE);
        System.out.printf("   线程池队列: ArrayBlockingQueue(%d)%n", THREAD_POOL_QUEUE_SIZE);
        System.out.printf("   核心线程: %d, 最大线程: %d%n", CORE_THREADS, MAX_THREADS);
        System.out.printf("   消费者线程: %d%n", CONSUMER_THREADS);
        System.out.println();
    }
    
    /**
     * 接收信令 - WebSocket回调调用此方法
     * 采用智能背压控制策略
     */
    public void receiveSignal(Signal signal) {
        monitor.recordReceived();  // 统计接收总数
        
        if (!receiveQueue.offer(signal)) {
            // 队列已满，根据信令优先级决定处理策略
            if (signal.getType().getPriority() >= 2) {
                // 高优先级信令(送礼、关注、进场)：尝试强制入队
                try {
                    receiveQueue.put(signal);  // 阻塞等待，确保重要信令不丢失
                    monitor.recordEnqueuedToReceive();  // 统计成功入队
                    System.out.printf("⚠️ 高优先级信令强制入队: %s%n", signal.getType().getDesc());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    monitor.recordDroppedAtReceive(signal);  // 统计接收阶段丢弃
                    System.out.printf("❌ 高优先级信令入队失败: %s%n", signal.getType().getDesc());
                }
            } else {
                // 低优先级信令(评论、点赞)：直接丢弃
                monitor.recordDroppedAtReceive(signal);  // 统计接收阶段丢弃
                 System.out.printf("🗑️ 低优先级信令丢弃: %s%n", signal.getType().getDesc());
            }
        } else {
            monitor.recordEnqueuedToReceive();  // 统计成功入队
        }
    }
    
    /**
     * 消费者循环 - 核心处理逻辑
     * 采用take()阻塞获取 + 批量处理 + 优先级排序
     */
    private void consumerLoop() {
        List<Signal> batch = new ArrayList<>(BATCH_SIZE);
        String threadName = Thread.currentThread().getName();
        
        System.out.printf("🚀 消费者线程启动: %s%n", threadName);
        
        while (running) {
            try {
                // 1. 使用take()阻塞等待第一个信令 - 避免CPU空转
                Signal firstSignal = receiveQueue.take();
                monitor.recordDequeued();  // 统计从接收队列取出
                batch.add(firstSignal);
                
                // 2. 批量获取更多信令 - 提升吞吐量
                int additionalCount = receiveQueue.drainTo(batch, BATCH_SIZE - 1);
                for (int i = 0; i < additionalCount; i++) {
                    monitor.recordDequeued();  // 统计批量取出的每一个
                }
                
                // 3. 按优先级排序 - 确保重要信令优先处理
                batch.sort((s1, s2) -> Integer.compare(
                    s2.getType().getPriority(), s1.getType().getPriority()));
                
                // 4. 提交到线程池批量处理
                for (Signal signal : batch) {
                    monitor.updateMaxThreads(executor.getPoolSize());
                    try {
                        executor.execute(() -> processSignal(signal));
                        monitor.recordSubmittedToExecutor();  // 统计提交成功
                    } catch (RejectedExecutionException e) {
                        // 注意：由于使用CallerRunsPolicy，实际上不会抛出此异常
                        // 但保留此代码以防将来修改拒绝策略
                        monitor.recordDroppedAtExecutor(signal);
                    }
                }
                
                // 5. 清空批次，准备下一轮
                batch.clear();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("⏹️ 消费者线程中断: %s%n", threadName);
                break;
            }
        }
        
        System.out.printf("✅ 消费者线程结束: %s%n", threadName);
    }
    
    /**
     * 处理单个信令 - 业务逻辑处理
     */
    private void processSignal(Signal signal) {
        try {
            CommonOperation.handleSignal(signal);
            
            // 记录处理成功和延迟
            long latency = System.currentTimeMillis() - signal.getCreateTime();
            monitor.recordProcessed(signal, latency);
            
        } catch (Exception e) {
            System.out.printf("❌ 信令处理异常: %s, 错误: %s%n", signal, e.getMessage());
            monitor.recordFailed(signal);  // 统计处理失败
        }
    }
    

    /**
     * 获取处理器状态信息
     */
    public String getStatus() {
        return String.format(Locale.getDefault(),
            "接收队列: %d/%d, 线程池队列: %d/%d, 活跃线程: %d/%d",
            receiveQueue.size(), RECEIVE_QUEUE_SIZE,
            executor.getQueue().size(), THREAD_POOL_QUEUE_SIZE,
            executor.getActiveCount(), executor.getMaximumPoolSize()
        );
    }
    
    /**
     * 优雅关闭处理器
     */
    public void shutdown() {
        System.out.println("🛑 开始关闭信令处理器...");
        
        // 1. 停止接收新信令
        running = false;
        
        // 2. 中断消费者线程
        for (Thread consumer : consumers) {
            consumer.interrupt();
        }
        
        // 3. 等待消费者线程结束
        for (Thread consumer : consumers) {
            try {
                consumer.join(2000);
                System.out.printf("✅ 消费者线程已结束: %s%n", consumer.getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("⚠️ 等待消费者线程结束超时: %s%n", consumer.getName());
            }
        }
        
        // 4. 关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                System.out.println("⚠️ 强制关闭线程池");
            } else {
                System.out.println("✅ 线程池优雅关闭完成");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.printf("\n=== %s 完整统计报告 ===%n", this.getClass().getSimpleName());
        // 5. 打印详细的生命周期统计报告
        monitor.printDetailedReport(
                receiveQueue.size(),
                executor.getQueue().size(),
                executor.getActiveCount());
        
        // 同时打印性能报告
        monitor.printPerformanceReport("直播间信令处理器");
        
        System.out.println("✅ 信令处理器关闭完成");
    }
    
    /**
     * 测试方法 - 模拟直播间突发流量场景
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🎮 直播间信令处理器测试开始");
        System.out.println("模拟场景: 10人突增到5000人，产生10000个信令");
        System.out.println();
        
        // 创建处理器
        LiveStreamSignalProcessor processor = new LiveStreamSignalProcessor();
        
        // 生成测试信令
        List<Signal> signals = CommonOperation.generateSignals(SIGN_COUNT);
        System.out.printf("📦 生成测试信令: %d 个%n", signals.size());
        
        // 模拟高并发接收
        long startTime = System.currentTimeMillis();
        for (Signal signal : signals) {
            processor.receiveSignal(signal);
        }
        System.out.printf("📨 信令接收完成，耗时: %d ms%n", System.currentTimeMillis() - startTime);
        
        // 等待处理完成
        System.out.println("⏳ 等待信令处理完成...");
        Thread.sleep(5000);
        
        // 打印实时状态
        System.out.printf("📊 当前状态: %s%n", processor.getStatus());
        
        // 继续等待
        Thread.sleep(3000);
        
        // 关闭处理器
        processor.shutdown();
    }
    
}