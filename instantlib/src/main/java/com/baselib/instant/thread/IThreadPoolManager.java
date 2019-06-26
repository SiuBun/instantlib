package com.baselib.instant.thread;

/**
 * 线程池管理对象需实现的接口
 * <p>
 * 指定操作
 *
 * @author wsb
 */
public interface IThreadPoolManager {

    /**
     * 执行任务
     *
     * @param task 目标task
     */
    void execute(Runnable task);

    /**
     * 执行取消
     *
     * @param task 目标task
     */
    void cancel(Runnable task);

    /**
     * 资源回收
     * */
    void cleanUp();
}
