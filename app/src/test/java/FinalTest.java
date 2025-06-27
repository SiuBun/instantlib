import com.baselib.queue.simple.CommonOperation;
import com.baselib.queue.simple.LiveStreamSignalProcessor;
import com.baselib.queue.entity.Signal;
import com.baselib.queue.entity.SignalType;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinalTest {
    @Test
    public void finalTest() {
        System.out.println("🎮 直播间信令处理器测试开始");
        System.out.println("模拟场景: 10人突增到5000人，产生10000个信令");
        System.out.println();

        final int SIGNAL_COUNT = 5000;

        // 创建处理器
        LiveStreamSignalProcessor processor = new LiveStreamSignalProcessor();

        // 生成测试信令
        List<Signal> signals = CommonOperation.generateSignals(SIGNAL_COUNT);
        System.out.printf("📦 生成测试信令: %d 个%n", signals.size());

        // 打印信令分布
        System.out.println("📊 信令分布:");
        Map<SignalType, Long> distribution = signals.stream()
                .collect(Collectors.groupingBy(Signal::getType, Collectors.counting()));
        distribution.forEach((type, count) ->
                System.out.printf("   %s: %d (%.1f%%)%n", type.getDesc(), count, count * 100.0 / SIGNAL_COUNT));
        System.out.println();

        // 模拟高并发接收
        long startTime = System.currentTimeMillis();
        for (Signal signal : signals) {
            processor.receiveSignal(signal);
        }
        System.out.printf("📨 信令接收完成，耗时: %d ms%n", System.currentTimeMillis() - startTime);

        // 等待处理完成
        System.out.println("⏳ 等待信令处理完成...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 打印实时状态
        System.out.printf("📊 当前状态: %s%n", processor.getStatus());

        // 继续等待
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 关闭处理器
        processor.shutdown();
    }

}
