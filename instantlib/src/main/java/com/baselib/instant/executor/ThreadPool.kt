package com.baselib.instant.executor

import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * 简易线程池代理操作类
 *
 * 持有一个线程池对象进行线程操作
 *
 * @author wsb
 */
class ThreadPool constructor(private val threadCapacity: Int) {

    /**
     * 所依赖的线程池本身
     */
    private var executorService: ExecutorService? = null

    private val threadFactory = { r: Runnable ->
        val thread = Thread(r)
        thread.priority = Thread.NORM_PRIORITY
        thread
    }

    companion object {
        /**
         * 默认线程池容量
         */
        const val THREAD_POOL_CAPACITY_DEF = 2
    }

    constructor() : this(THREAD_POOL_CAPACITY_DEF) {
        executorService = ScheduledThreadPoolExecutor(threadCapacity, threadFactory)
    }

    fun submit(r: Runnable) {
        open()
        executorService!!.submit(r)
    }

    /**
     * 关闭线程池,清除已submit但未执行的任务
     */
    fun shutDown() {
        executorService!!.shutdownNow()
    }

    /**
     * 清除已submit，但未执行的任务
     */
    fun clear(){
        if (executorService is ThreadPoolExecutor){
            (executorService as ThreadPoolExecutor).queue.clear()
        }
    }

    /**
     * 检查并开启线程池
     */
    private fun open() {
        if (null == executorService || executorService!!.isShutdown) {
            executorService = ScheduledThreadPoolExecutor(threadCapacity, threadFactory)
        }
    }

}