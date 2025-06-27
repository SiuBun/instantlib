import com.baselib.queue.CommonOperation;
import com.baselib.queue.Signal;
import com.baselib.queue.SignalType;
import com.baselib.queue.Step4_ReceiveQueueDrainMultiConsumer;

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

        Step4_ReceiveQueueDrainMultiConsumer demo = new Step4_ReceiveQueueDrainMultiConsumer();

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] array = getReceiveExecutorQueueProcessors();


        // 3. 测试最终优化处理器
        for (int i = 0; i < array.length; i++) {
            Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor processor = array[i];
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

    private static Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] getReceiveExecutorQueueProcessors() {
        final int RECEIVE_QUEUE_SIZE = 500;
        final int THREAD_POOL_QUEUE_SIZE = 400;

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedLinkedProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedArrayProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor linkedSyncProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new LinkedBlockingQueue<>(),
                new SynchronousQueue<>());

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arrayArrayProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arrayLinkedProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor arraySyncProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new ArrayBlockingQueue<>(RECEIVE_QUEUE_SIZE),
                new SynchronousQueue<>());

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncSyncProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new SynchronousQueue<>());

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncArrayProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));

        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor syncLinkedProcessor = new Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor(
                new SynchronousQueue<>(),
                new LinkedBlockingQueue<>(THREAD_POOL_QUEUE_SIZE));


        Step4_ReceiveQueueDrainMultiConsumer.ReceiveExecutorQueueProcessor[] array = {
                linkedLinkedProcessor, linkedArrayProcessor, linkedSyncProcessor,
                arrayArrayProcessor, arrayLinkedProcessor, arraySyncProcessor,
                syncSyncProcessor, syncLinkedProcessor, syncArrayProcessor,
        };
        return array;
    }


}
