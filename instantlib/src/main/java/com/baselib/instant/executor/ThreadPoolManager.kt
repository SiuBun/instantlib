package com.baselib.instant.executor

import android.os.Build
import android.util.Log
import java.util.*
import java.util.concurrent.*

/**
 * 线程管理对象
 *
 * @author wsb
 */
class ThreadPoolManager constructor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long,
                                    unit: TimeUnit, isPriority: Boolean, listener: ITaskExecuteListener?) : IThreadPoolManager {


    private var name: String? = null

    private val bytesLock = ByteArray(0)

    /**
     * 真正执行线程处理的线程池
     */
    private var workThreadPool: ThreadPoolExecutor

    /**
     * 保存等待任务的链表队列
     */
    private val waitTasksQueue: Queue<Runnable>

    /**
     * 任务被拒绝执行的处理器
     */
    private var rejectedExecutionHandler: RejectedExecutionHandler? = null


    companion object {

        private const val DEFAULT_CORE_POOL_SIZE = 4
        private const val DEFAULT_MAXIMUM_POOL_SIZE = 4
        private const val DEFAULT_KEEP_ALIVE_TIME: Long = 0
        private val DEFAULT_TIME_UNIT = TimeUnit.SECONDS

        fun buildInstance(threadPoolManagerName: String,
                          corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit): ThreadPoolManager? {
            return buildInstance(threadPoolManagerName, corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, false)
        }

        fun buildInstance(threadPoolManagerName: String,
                          corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit,
                          isPriority: Boolean): ThreadPoolManager? {
            return buildInstance(threadPoolManagerName, corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, isPriority, null)
        }

        fun buildInstance(threadPoolManagerName: String?,
                          corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit,
                          isPriority: Boolean = false, listener: ITaskExecuteListener?): ThreadPoolManager? {
            return if (threadPoolManagerName == null || "" == threadPoolManagerName.trim { it <= ' ' }
                    || corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize
                    || keepAliveTime < 0) {
                null
            } else {
                val threadPoolManager = ThreadPoolManager(corePoolSize,
                        maximumPoolSize, keepAliveTime, unit, isPriority, listener)
                threadPoolManager.name = threadPoolManagerName

                threadPoolManager
            }
        }
    }

    constructor() : this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME,
            DEFAULT_TIME_UNIT, false, null)

    init {
        waitTasksQueue = ConcurrentLinkedQueue()
        initRejectedExecutionHandler()

        val queue:BlockingQueue<Runnable> =
                if (isPriority) {
                    PriorityBlockingQueue<Runnable>(16)
                } else {
                    LinkedBlockingQueue<Runnable>(16)
                }

        val threadFactory = ThreadFactory { r: Runnable? ->
            val thread = Thread(r)
            thread.priority = Thread.NORM_PRIORITY
            thread
        }

        if (listener == null) {
            workThreadPool = ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, queue, threadFactory, rejectedExecutionHandler)
        } else {
            workThreadPool = object : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, queue, threadFactory, rejectedExecutionHandler) {
                override fun beforeExecute(t: Thread?, r: Runnable?) {
                    listener.beforeExecute(t, r)
                }

                override fun afterExecute(r: Runnable?, t: Throwable?) {
                    listener.afterExecute(r, t)
                }
            }
        }
    }

    /**
     * 初始化任务被拒绝执行的处理器的方法
     */
    fun initRejectedExecutionHandler() {
        rejectedExecutionHandler = RejectedExecutionHandler { r, _ ->
            synchronized(bytesLock) {
                waitTasksQueue.offer(r)
            }
        }
    }

    /**
     * 获取执行等待队列任务的Runnable
     *
     * @return 执行所有等待队列的任务
     */
    internal fun getScheduleRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                synchronized(this) {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                    executeWaitTask()
                }
            }
        }
    }

    private fun executeWaitTask() {
        synchronized(bytesLock) {
            if (hasMoreWaitTask()) {
                val runnable = waitTasksQueue.poll()
                if (runnable != null) {
                    execute(runnable)
                }
            }
        }
    }

    /**
     * 是否还有等待任务的方法
     *
     * @return
     */
    fun hasMoreWaitTask(): Boolean {
        return !waitTasksQueue.isEmpty()
    }

    /**
     * 执行任务的方法
     *
     * @param task 目标任务
     */
    override fun execute(task: Runnable) {
        workThreadPool.execute(task)
    }


    /**
     * 取消任务
     * <p>
     * 如果等待队列中存在,那么队列移除该任务后再由线程池移除
     *
     * @param task 准备移除的任务
     */
    override fun cancel(task: Runnable) {
        synchronized(bytesLock) {
            if (waitTasksQueue.contains(task)) {
                waitTasksQueue.remove(task)
            }
        }
        workThreadPool.remove(task)
    }

    /**
     * 清理方法
     *
     * 步骤
     *
     * 1.关闭线程池
     * 2.置空被拒处理器
     * 3.清空队列
     */
    override fun cleanUp() {
        if (!workThreadPool.isShutdown) {
            try {
                workThreadPool.shutdownNow()
            } catch (e: Exception) {
                // TODO: handle exception
            }

        }
        rejectedExecutionHandler = null
//		if (sScheduledExecutorService != null) {
//			if (!sScheduledExecutorService.isShutdown()) {
//				try {
//					sScheduledExecutorService.shutdownNow();
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
//			}
//			sScheduledExecutorService = null;
//		}
//		sScheduledRunnable = null;
        synchronized(bytesLock) {
            waitTasksQueue.clear()
        }
    }

    /**
     * 移除所有的任务
     *
     *
     * 线程池未关闭的情况下，将线程池中的任务都移除掉
     */
    fun removeAllTask() {
        // 如果取task过程中task队列数量改变了会抛异常
        try {
            if (!workThreadPool.isShutdown) {
                val tasks = workThreadPool.queue
                for (task in tasks) {
                    workThreadPool.remove(task)
                }
            }
        } catch (e: Throwable) {
            Log.e("ThreadPoolManager", "removeAllTask " + e.message)
        }

    }

    fun isShutdown(): Boolean {
        return workThreadPool.isShutdown
    }

    fun setThreadFactory(factory: ThreadFactory) {
        workThreadPool.threadFactory = factory
    }

    fun allowCoreThreadTimeOut(allow: Boolean) {
        if (Build.VERSION.SDK_INT > 8) {
            workThreadPool.allowCoreThreadTimeOut(allow)
        }
    }


    /**
     * 线程任务执行监听
     */
    interface ITaskExecuteListener {
        /**
         * 同步 ThreadPoolExecutor:beforeExecute(Thread, Runnable)回调
         */
        fun beforeExecute(thread: Thread?, task: Runnable?)

        /**
         * 同步 ThreadPoolExecutor#afterExecute(Thread, Runnable)}回调
         */
        fun afterExecute(task: Runnable?, throwable: Throwable?)
    }
}