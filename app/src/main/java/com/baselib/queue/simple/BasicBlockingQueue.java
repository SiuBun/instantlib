package com.baselib.queue.simple;

import com.baselib.queue.entity.PriorityTask;
import com.baselib.queue.entity.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 第一步：基础阻塞队列学习Demo
 * <p>
 * 目标：了解Java中不同BlockingQueue的基本特性和使用方法
 * <p>
 * 涉及队列：
 * 1. ArrayBlockingQueue - 基于数组的有界阻塞队列
 * 2. LinkedBlockingQueue - 基于链表的可选有界阻塞队列
 * 3. SynchronousQueue - 不存储元素的阻塞队列
 * 4. PriorityBlockingQueue - 具有优先级的无界阻塞队列
 * <p>
 * 学习重点：
 * - 各种队列的容量特性
 * - 阻塞和非阻塞操作的区别
 * - 生产者消费者基本模式
 */
public class BasicBlockingQueue {

    /**
     * 通用的基础操作测试
     * 测试队列的基本添加和获取操作
     */
    public static void testBasicOperations(BlockingQueue<Task> queue, String queueName) {
        System.out.println("=== 测试" + queueName + "基础操作 ===");

        try {
            // 测试添加操作
            System.out.println("1. 测试添加操作：");
            queue.put(new Task(1, "任务1"));  // 阻塞添加
            queue.offer(new Task(2, "任务2")); // 非阻塞添加
            System.out.println("   添加两个任务成功，当前队列大小：" + queue.size());

            // 添加第三个任务
            boolean success = queue.offer(new Task(3, "任务3"), 1, TimeUnit.SECONDS);
            System.out.println("   添加第三个任务：" + (success ? "成功" : "失败"));
            System.out.println("   当前队列大小：" + queue.size());

            // 尝试添加第四个任务（如果队列有界且已满会失败）
            success = queue.offer(new Task(4, "任务4"));
            System.out.println("   添加第四个任务：" + (success ? "成功" : "失败（队列可能已满）"));

            // 测试获取操作
            System.out.println("\n2. 测试获取操作：");
            Task task = queue.take();  // 阻塞获取
            System.out.println("   阻塞获取：" + task);

            task = queue.poll();       // 非阻塞获取
            System.out.println("   非阻塞获取：" + task);

            task = queue.poll(1, TimeUnit.SECONDS);  // 超时获取
            System.out.println("   超时获取：" + task);

            // 尝试从空队列获取
            task = queue.poll();
            System.out.println("   从空队列获取：" + task);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("操作被中断：" + e.getMessage());
        }

        System.out.println(queueName + "基础操作测试完成\n");
    }

    /**
     * 批量操作测试
     * 测试队列的批量添加和获取
     */
    public static void testBatchOperations(BlockingQueue<Task> queue, String queueName, int taskCount) {
        System.out.println("=== 测试" + queueName + "批量操作 ===");

        try {
            System.out.println("1. 批量添加任务：");

            long startOfferTime = System.currentTimeMillis();
            // 添加多个任务
            for (int i = 1; i <= taskCount; i++) {
                boolean success = queue.offer(new Task(i, "批量任务" + i));
                if (!success) {
                    System.out.println("   任务" + i + "添加失败（队列可能已满）");
                }
            }
            System.out.println("   批量添加完成，队列大小：" + queue.size());
            long offerDuration = System.currentTimeMillis() - startOfferTime;
            System.out.printf("   总offer时间: %d ms (%.1f 秒)%n", offerDuration, offerDuration / 1000.0);

            System.out.println("\n2. 批量获取任务：");
            long startPollTime = System.currentTimeMillis();
            while (!queue.isEmpty()) {
                Task task = queue.poll();
                System.out.println("   获取：" + task);
            }
            long pollDuration = System.currentTimeMillis() - startPollTime;
            System.out.printf("   总poll时间: %d ms (%.1f 秒)%n", pollDuration, pollDuration / 1000.0);

        } catch (Exception e) {
            System.out.println("操作异常：" + e.getMessage());
        }

        System.out.println(queueName + "批量操作测试完成\n");
    }

    /**
     * 生产者消费者并发测试
     * 测试队列在并发环境下的表现
     */
    public static void testProducerConsumer(BlockingQueue<Task> queue, String queueName, int taskCount) {
        System.out.println("=== 测试" + queueName + "生产者消费者模式 ===");
        System.out.printf("任务数量: %d%n", taskCount);

        long startTime = System.currentTimeMillis();

        // 创建生产者线程
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    Task task = new Task(i, "并发任务" + i);
                    System.out.println("   生产者准备发送：" + task);
                    queue.put(task);  // 会阻塞直到能够放入队列
                    System.out.println("   生产者成功发送：" + task);
                    Thread.sleep(1000);
                }
                System.out.println("   生产者完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // 创建消费者线程
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    System.out.println("   消费者准备接收...");
                    Task task = queue.take();  // 会阻塞直到有任务可用
                    System.out.println("   消费者成功接收：" + task);
                    Thread.sleep(1500);
                }
                System.out.println("   消费者完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        // 启动线程
        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.printf("✅ %s生产者消费者测试完成%n", queueName);
        System.out.printf("   总执行时间: %d ms (%.1f 秒)%n", duration, duration / 1000.0);
        System.out.println();
    }


    /**
     * 优先级队列测试
     * 测试队列的优先级排序功能
     */
    public static void testPriorityOperations(BlockingQueue<PriorityTask> queue, String queueName) {
        System.out.println("=== 测试" + queueName + "优先级操作 ===");

        try {
            System.out.println("1. 添加不同优先级的任务：");

            // 随机添加不同优先级的任务
            queue.offer(new PriorityTask(2, "中优先级任务"));
            queue.offer(new PriorityTask(5, "高优先级任务"));
            queue.offer(new PriorityTask(1, "低优先级任务"));
            queue.offer(new PriorityTask(3, "中高优先级任务"));

            System.out.println("   添加了4个任务，队列大小：" + queue.size());

            System.out.println("\n2. 按优先级顺序获取任务：");
            while (!queue.isEmpty()) {
                PriorityTask task = queue.poll();
                System.out.println("   获取：" + task);
            }

        } catch (Exception e) {
            System.out.println("操作异常：" + e.getMessage());
        }

        System.out.println(queueName + "优先级操作测试完成\n");
    }

    /**
     * 性能基准测试
     * 测试队列在高负载下的性能表现
     */
    public static void performanceBenchmark(BlockingQueue<Task> queue, String queueName, int taskCount) {
        System.out.println("=== " + queueName + "性能基准测试 ===");
        System.out.printf("任务数量: %d%n", taskCount);

        long startTime = System.currentTimeMillis();

        // 生产者
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    Task task = new Task(i, "性能任务" + i);
                    queue.put(task);
                    if (i % 1000 == 0) {
                        System.out.printf("   已生产: %d/%d%n", i, taskCount);
                    }
                }
                System.out.println("   生产者完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // 消费者
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= taskCount; i++) {
                    Task task = queue.take();
                    if (i % 1000 == 0) {
                        System.out.printf("   已消费: %d/%d%n", i, taskCount);
                    }
                }
                System.out.println("   消费者完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;
        double throughput = taskCount / (duration / 1000.0);

        System.out.printf("✅ %s性能测试完成%n", queueName);
        System.out.printf("   总耗时: %d ms%n", duration);
        System.out.printf("   吞吐量: %.1f 任务/秒%n", throughput);
        System.out.println();
    }

    //    public static void main(String[] args) {
//        System.out.println("🎯 第一步：基础阻塞队列学习Demo");
//        System.out.println("目标：了解Java中不同BlockingQueue的基本特性\n");
//
//        // 依次测试各种队列
//        testArrayBlockingQueue();
//        testLinkedBlockingQueue();
//        testSynchronousQueue();
//        testPriorityBlockingQueue();
//        simpleProducerConsumerExample();
//
//        System.out.println("🎉 基础阻塞队列学习完成！");
//        System.out.println("\n📚 学习总结：");
//        System.out.println("1. ArrayBlockingQueue：有界、数组实现、FIFO");
//        System.out.println("2. LinkedBlockingQueue：可选有界、链表实现、FIFO");
//        System.out.println("3. SynchronousQueue：无存储、直接传递");
//        System.out.println("4. PriorityBlockingQueue：无界、优先级排序");
//        System.out.println("\n💡 关键点：");
//        System.out.println("- put/take：阻塞操作");
//        System.out.println("- offer/poll：非阻塞操作");
//        System.out.println("- offer/poll(timeout)：超时操作");
//    }
}