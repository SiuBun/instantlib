import com.baselib.queue.PriorityTask
import com.baselib.queue.Step1_BasicBlockingQueue
import com.baselib.queue.Task
import org.junit.Test
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.SynchronousQueue

class StepTest {

    /**
     * 对比测试：不同队列的基础操作性能
     */
    @Test
    fun testQueueComparison() {
        println("🔄 对比测试：不同队列的基础操作")
        println("=".repeat(50))

        // 测试ArrayBlockingQueue
        val arrayQueue = ArrayBlockingQueue<Task>(3)
        Step1_BasicBlockingQueue.testBasicOperations(arrayQueue, "ArrayBlockingQueue")

        // 测试LinkedBlockingQueue（有界）
        val linkedBoundedQueue = LinkedBlockingQueue<Task>(3)
        Step1_BasicBlockingQueue.testBasicOperations(
            linkedBoundedQueue,
            "LinkedBlockingQueue(有界)"
        )

        // 测试LinkedBlockingQueue（无界）
        val linkedUnboundedQueue = LinkedBlockingQueue<Task>()
        Step1_BasicBlockingQueue.testBasicOperations(
            linkedUnboundedQueue,
            "LinkedBlockingQueue(无界)"
        )

        println("✅ 基础操作对比测试完成")
    }

    /**
     * 对比测试：不同队列的批量操作性能
     */
    @Test
    fun testBatchComparison() {
        println("📦 对比测试：不同队列的批量操作")
        println("=".repeat(50))

        // 测试ArrayBlockingQueue
        val arrayQueue = ArrayBlockingQueue<Task>(1000)
        Step1_BasicBlockingQueue.testBatchOperations(arrayQueue, "ArrayBlockingQueue", 1000)

        // 测试LinkedBlockingQueue
        val linkedQueue = LinkedBlockingQueue<Task>(1000)
        Step1_BasicBlockingQueue.testBatchOperations(linkedQueue, "LinkedBlockingQueue", 1000)

        // 测试SynchronousQueue
        val syncQueue = SynchronousQueue<Task>()
        Step1_BasicBlockingQueue.testBatchOperations(syncQueue, "SynchronousQueue", 1000)
        println("✅ 批量操作对比测试完成")
    }

    /**
     * 对比测试：不同队列的并发性能
     */
    @Test
    fun testConcurrencyComparison() {
        println("🚀 对比测试：不同队列的并发性能")
        println("=".repeat(50))

        // 测试ArrayBlockingQueue
        val arrayQueue = ArrayBlockingQueue<Task>(3)
        Step1_BasicBlockingQueue.testProducerConsumer(arrayQueue, "ArrayBlockingQueue", 5)

        // 测试LinkedBlockingQueue
        val linkedQueue = LinkedBlockingQueue<Task>(3)
        Step1_BasicBlockingQueue.testProducerConsumer(linkedQueue, "LinkedBlockingQueue", 5)

        // 测试SynchronousQueue
        val syncQueue = SynchronousQueue<Task>()
        Step1_BasicBlockingQueue.testProducerConsumer(syncQueue, "SynchronousQueue", 5)

        println("✅ 并发性能对比测试完成")
    }

    /**
     * 对比测试：优先级队列 vs 普通队列
     */
    @Test
    fun testPriorityComparison() {
        println("⭐ 对比测试：优先级队列 vs 普通队列")
        println("=".repeat(50))

        // 测试PriorityBlockingQueue
        val priorityQueue = PriorityBlockingQueue<PriorityTask>()
        Step1_BasicBlockingQueue.testPriorityOperations(priorityQueue, "PriorityBlockingQueue")

        println("✅ 优先级对比测试完成")
    }

    /**
     * 性能基准对比测试：不同队列的高负载性能
     */
    @Test
    fun testPerformanceBenchmark() {
        println("🏁 性能基准对比测试：高负载场景")
        println("=".repeat(50))

        val taskCount = 10000

        // 测试ArrayBlockingQueue
        val arrayQueue = ArrayBlockingQueue<Task>(1000)
        Step1_BasicBlockingQueue.performanceBenchmark(arrayQueue, "ArrayBlockingQueue", taskCount)

        // 测试LinkedBlockingQueue
        val linkedQueue = LinkedBlockingQueue<Task>(1000)
        Step1_BasicBlockingQueue.performanceBenchmark(linkedQueue, "LinkedBlockingQueue", taskCount)

        // 测试SynchronousQueue（较少任务数，因为每个put必须等待take）
        val syncQueue = SynchronousQueue<Task>()
        Step1_BasicBlockingQueue.performanceBenchmark(syncQueue, "SynchronousQueue", taskCount)

        println("✅ 性能基准对比测试完成")
        println("📊 结论：可以通过上述数据对比不同队列的性能特征")
    }



}