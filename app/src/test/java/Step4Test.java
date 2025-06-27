import com.baselib.queue.simple.CommonOperation;
import com.baselib.queue.entity.Signal;
import com.baselib.queue.entity.SignalType;
import com.baselib.queue.simple.ReceiveQueueDrainMultiConsumer;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

public class Step4Test {
    @Test
    public void step4Test() {
        System.out.println("🎯 第四步：多种队列组合的性能对比测试");
        System.out.println("场景：直播间突发流量，10人突增到5000人，产生5000个信令");
        System.out.println("目标：对比9种不同队列组合的性能表现");
        System.out.println("测试内容：LinkedBlockingQueue/ArrayBlockingQueue/SynchronousQueue的各种组合");
        System.out.println("=".repeat(70));

        final int SIGNAL_COUNT = 5000;


        // 生成测试数据
        System.out.printf("📦 生成测试信令: %d 个%n", SIGNAL_COUNT);
        List<Signal> signals = CommonOperation.generateSignals(SIGNAL_COUNT);

        // 打印信令分布
        System.out.println("📊 信令分布:");
        Map<SignalType, Long> distribution = signals.stream()
                .collect(Collectors.groupingBy(Signal::getType, Collectors.counting()));
        distribution.forEach((type, count) ->
                System.out.printf("   %s: %d (%.1f%%)%n", type.getDesc(), count, count * 100.0 / SIGNAL_COUNT));
        System.out.println();

        ReceiveQueueDrainMultiConsumer demo = new ReceiveQueueDrainMultiConsumer();

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] array = getReceiveExecutorQueueProcessors();


        // 3. 测试最终优化处理器
        for (int i = 0; i < array.length; i++) {
            ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor processor = array[i];
            int finalI = i;
            String planName = String.format("第%d个方案", i);
            demo.runProcessorTest(planName, () -> {
                System.out.printf("✅ %s处理器启动%n", planName);
                processor.start();

                for (Signal signal : signals) {
                    processor.receiveSignal(signal);
                }

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                }
                processor.shutdown(finalI);
            });
        }

        System.out.println("🎉 第四步测试完成！");
    }

    private static ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] getReceiveExecutorQueueProcessors() {
        final int RECEIVE_QUEUE_SIZE = 500;
        final int THREAD_POOL_QUEUE_SIZE = 400;

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedLinkedProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedArrayProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedSyncProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(),
                new SynchronousQueue<>());

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arrayArrayProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arrayLinkedProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arraySyncProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new SynchronousQueue<>());

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncSyncProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new SynchronousQueue<>());

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncArrayProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncLinkedProcessor = new ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));


        ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] array = {
                linkedLinkedProcessor, linkedArrayProcessor, linkedSyncProcessor,
                arrayArrayProcessor, arrayLinkedProcessor, arraySyncProcessor,
                syncSyncProcessor, syncLinkedProcessor, syncArrayProcessor,
        };
        return array;
    }


}
