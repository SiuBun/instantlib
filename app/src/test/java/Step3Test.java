import com.baselib.queue.simple.ProducerConsumerDrainTo;

import org.junit.Test;

public class Step3Test {
    @Test
    public void step3Test() {
        System.out.println("🎯 第三步：Producer-Consumer模式优化");
        System.out.println("背景：分离信令接收和处理，引入友商的批量处理方案");
        System.out.println("目标：通过策略模式统一处理器，对比基础方案和批量处理方案的性能差异");
        System.out.println();

        ProducerConsumerDrainTo demo = new ProducerConsumerDrainTo();
        // 使用统一的Producer-Consumer处理器，支持两种消费者策略：
        System.out.println("🧪 统一处理器测试 - 支持多种消费者策略");
        System.out.println("   BasicConsumerStrategy: 逐个处理信令，使用take()阻塞获取");
        System.out.println("   BatchConsumerStrategy: take() + drainTo批量获取 + 优先级排序");
        System.out.println();
        demo.testProducerConsumer();

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

}
