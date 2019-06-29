package com.baselib.instant.thread

import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 优先线程池
 *
 * @author wsb
 */
class PriorityThreadPool constructor(private val threadCapacity:Int){

    companion object {
        /**
         * 默认线程池容量
         * */
        const val THREAD_POOL_CAPACITY_DEF = 2
    }

    var threadPool:ThreadPoolExecutor?=null



    constructor():this(THREAD_POOL_CAPACITY_DEF)

    init {
        val queue:PriorityBlockingQueue<Runnable> = PriorityBlockingQueue(threadCapacity)

        val function = { r: Runnable ->
            val thread = Thread(r)
            thread.priority = Thread.MAX_PRIORITY
            thread
        }

        val threadFactory =ThreadFactory{r: Runnable? ->
            val thread = Thread(r)
            thread.priority = Thread.MAX_PRIORITY
            thread
        }

        threadPool = ThreadPoolExecutor(threadCapacity,threadCapacity,30, TimeUnit.SECONDS, queue,threadFactory)
    }

    /**
     * 提交任务请求
     * @param r 目标任务
     * 备注:该Runnable实现类必须同时实现 Comparable<T super Runnable>
     */
    fun submit(r:Runnable){
        open()
        threadPool!!.execute(r)
    }

    /**
     * 关闭线程池,清除已submit但未执行的任务
     */
    fun shutdown(){
        threadPool!!.shutdownNow()
    }

    /**
     * 清除已submit，但未执行的任务
     */
    fun clear(){
        if(threadPool is ThreadPoolExecutor){
            (threadPool as ThreadPoolExecutor).queue.clear()
        }
    }

    /**
     * 从排队任务列表里删除
     * @param r 目标任务
     */
    fun remove(r: Runnable) {
        threadPool!!.remove(r)
    }

    /**
     * 检查并开启线程池
     */
    private fun open() {
        if (null == threadPool || threadPool!!.isShutdown) {
            val queue = PriorityBlockingQueue<Runnable>(threadCapacity)
            threadPool = ThreadPoolExecutor(threadCapacity, threadCapacity, 30, TimeUnit.SECONDS, queue)
        }
    }
}