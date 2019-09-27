package com.baselib.instant.executor

import java.util.concurrent.TimeUnit

/**
 * 扩展的线程执行对象
 *
 * @author wsb
 */
internal class ExtendThreadExecutor(private val poolName: String, corePoolSize: Int, private val defaultMaxPoolSize: Int,
                                    private val keepAliveTime: Int) : AbstractThreadExecutor() {
    private val corePoolSize: Int = if (corePoolSize > defaultMaxPoolSize) defaultMaxPoolSize else corePoolSize

    private val taskExecuteListener: ThreadPoolManager.ITaskExecuteListener = object : ThreadPoolManager.ITaskExecuteListener {

        override fun beforeExecute(thread: Thread?, task: Runnable?) {
            if (task is NamedTask) {
                task.threadName
                thread?.name = task.threadName
                thread?.priority = task.priority
            }
        }

        override fun afterExecute(task: Runnable?, throwable: Throwable?) {

        }

    }

    public override fun initThreadPoolManager(): IThreadPoolManager {
        val manager = ThreadPoolManager.buildInstance(poolName, corePoolSize,
                defaultMaxPoolSize, keepAliveTime.toLong(), TimeUnit.SECONDS, false,
                taskExecuteListener)
        manager!!.allowCoreThreadTimeOut(true)
        return manager
    }

}