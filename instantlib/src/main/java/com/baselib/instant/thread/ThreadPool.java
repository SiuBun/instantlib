package com.baselib.instant.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 简易线程池代理操作类
 * <p>
 * 持有一个线程池对象进行线程操作
 *
 * @author wsb
 */
public class ThreadPool {
    /**
     * 默认线程池容量
     */
    public static final int THREADPOOL_CAPACITY_DEF = 2;
    /**
     * 所依赖的线程池本身
     */
    private ExecutorService mExecutorService;
    private int mThreadCapacity;
    private ThreadFactory mThreadFactory = r -> {
        Thread thread = new Thread(r);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    };

    public ThreadPool() {
        this(THREADPOOL_CAPACITY_DEF);
    }

    /**
     * @param threadCapacity 线程池容量
     */
    public ThreadPool(int threadCapacity) {
        mThreadCapacity = threadCapacity;
        mExecutorService = new ScheduledThreadPoolExecutor(mThreadCapacity, mThreadFactory);
    }

    public void submit(Runnable r) {
        open();
        mExecutorService.submit(r);
    }

    /**
     * 关闭线程池,清除已submit但未执行的任务
     */
    public void shutDown() {
        mExecutorService.shutdownNow();
    }

    /**
     * 清除已submit，但未执行的任务
     */
    public void clear() {
        if (mExecutorService instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) mExecutorService).getQueue().clear();
        }
    }

    /**
     * 检查并开启线程池
     */
    private void open() {
        if (null == mExecutorService || mExecutorService.isShutdown()) {
            mExecutorService = new ScheduledThreadPoolExecutor(mThreadCapacity, mThreadFactory);
        }
    }
}
