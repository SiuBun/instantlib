package com.baselib.instant.executor

/**
 * 线程池管理对象需实现的接口
 *
 * 指定操作
 *
 * @author wsb
 */
interface IThreadPoolManager {
    /**
     * 执行任务
     *
     * @param task 目标task
     */
    fun execute(task: Runnable)

    /**
     * 执行取消
     *
     * @param task 目标task
     */
    fun cancel(task: Runnable)

    /**
     * 资源回收
     */
    fun cleanUp()
}