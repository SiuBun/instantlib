package com.baselib.instant.thread;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 优先线程池
 *
 * @author chenchongji
 * 2016年3月15日
 */
public class PriorityThreadPool {
    /**
     * 默认线程池容量
     * */
    public static final int THREADPOOL_CAPACITY_DEF = 2;

    /**
     * 线程池
     * */
    private ThreadPoolExecutor mThreadPool;

    private int mThreadCapacity;
    
    public PriorityThreadPool() {
    	this(THREADPOOL_CAPACITY_DEF);
    }
    
    /**
     * 线程池数量
     * @param threadCapacity
     */
    public PriorityThreadPool(int threadCapacity) {
    	mThreadCapacity = threadCapacity;
    	PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>(mThreadCapacity);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MAX_PRIORITY);
            return null;
        };
        mThreadPool = new ThreadPoolExecutor(mThreadCapacity, mThreadCapacity, 30, TimeUnit.SECONDS, queue,threadFactory);
    }
    
    /**
     * 提交任务请求
     * @param r
     * 备注:该Runnable实现类必须同时实现 Comparable<T super Runnable>
     */
    public void submit(Runnable r) {
        open();
//        mThreadPool.submit(r);
        //TODO 暂时解决使用submit方法会将Runnable转换成FutureTask从而丢失Comparable接口而引起的ClassCast异常
        mThreadPool.execute(r);
    }
    
    /**
     * 关闭线程池,清除已submit但未执行的任务
     */
    public void shutDown() {
//      mThreadPool.shutdown();
        mThreadPool.shutdownNow();
    }
    
    /**
     * 清除已submit，但未执行的任务
     */
    public void clear() {
    	mThreadPool.getQueue().clear();
    }
    
    /**
     * 从排队任务列表里删除
     * @param r
     */
    public void remove(Runnable r) {
    	mThreadPool.remove(r);
    }
    
    /**
     * 获取ThreadPoolExecutor
     * @return
     */
    public ThreadPoolExecutor getThreadPoolExecutor() {
    	return mThreadPool;
    }
    
    /**
     * 检查并开启线程池
     */
    private void open() {
        if (null == mThreadPool || mThreadPool.isShutdown()) {
        	PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>(mThreadCapacity);
        	mThreadPool = new ThreadPoolExecutor(mThreadCapacity, mThreadCapacity, 30, TimeUnit.SECONDS, queue);
        }
    }
}