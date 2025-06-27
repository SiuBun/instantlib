import com.baselib.queue.simple.CommonOperation;
import com.baselib.queue.entity.Signal;
import com.baselib.queue.simple.SignalByExecutor;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class Step2Test {

    /**
     * Step2 使用统一处理器创建不同队列配置的示例
     */
    @Test
    public void testSignalProcessorConvenientMethods() {
        System.out.println("🛠️ Step2 统一处理器不同队列配置示例");
        System.out.println("=".repeat(60));
        System.out.println("展示如何使用统一处理器传入不同队列参数，对比各种队列性能");
        System.out.println();

        int signalCount = 500;

        // 使用便捷方法创建不同处理器
        SignalByExecutor.SignalProcessor[] processors = {
                new SignalByExecutor.SignalProcessor(new SynchronousQueue<>()),
                new SignalByExecutor.SignalProcessor(new ArrayBlockingQueue<>(1000)),
                new SignalByExecutor.SignalProcessor(new LinkedBlockingQueue<>(1000)),
                new SignalByExecutor.SignalProcessor(new ArrayBlockingQueue<>(400)),
                new SignalByExecutor.SignalProcessor(new LinkedBlockingQueue<>(400)),
        };

        for (int i = 0; i < processors.length; i++) {
            SignalByExecutor.SignalProcessor processor = processors[i];
            System.out.printf("%d️⃣ 测试%s处理器%n", i + 1, processor.getQueueType());

            processor.start();
            List<Signal> signals = CommonOperation.generateSignals(signalCount);

            long startTime = System.currentTimeMillis();
            for (Signal signal : signals) {
                processor.processSignal(signal);
            }
            long sendTime = System.currentTimeMillis() - startTime;

            System.out.printf("   📨 发送耗时：%d ms%n", sendTime);

            System.out.printf("   📊 状态：活跃线程=%d，线程池大小=%d，队列大小=%d%n",
                    processor.getActiveThreadCount(), processor.getPoolSize(), processor.getQueueSize());

            // 等待处理
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            processor.shutdown();
            System.out.println("   ✅ 处理器已关闭");
            System.out.println();
        }

        System.out.println("✅ 统一处理器测试完成");
        System.out.println("💡 优势：统一的处理器设计，可以轻松传入不同的队列参数进行性能对比");
    }

    /**
     * Step2 高负载信令处理对比测试
     */
    @Test
    public void testHighLoadSignalProcessing() {
        System.out.println("🚀 Step2 高负载信令处理对比测试");
        System.out.println("=".repeat(60));
        System.out.println("场景：模拟真实直播间突发流量，5000个信令");
        System.out.println();

        int signalCount = 5000;  // 使用完整的信令数量

        // 只测试推荐的队列类型，避免SynchronousQueue在高负载下的问题
        System.out.println("🔥 ArrayBlockingQueue处理器 - 高负载测试");
        SignalByExecutor.testSignalProcessor(new ArrayBlockingQueue<>(1000), signalCount);
        System.out.println("=".repeat(50));

        System.out.println("🔥 LinkedBlockingQueue处理器 - 高负载测试");
        SignalByExecutor.testSignalProcessor(new LinkedBlockingQueue<>(1000), signalCount);
        System.out.println("=".repeat(50));

        System.out.println("✅ 高负载信令处理对比测试完成");
        System.out.println("📊 结论：在高负载场景下，有界队列表现更稳定");
        System.out.println("⚠️  注意：SynchronousQueue在高负载下可能导致线程数暴增，不推荐使用");
    }

}
