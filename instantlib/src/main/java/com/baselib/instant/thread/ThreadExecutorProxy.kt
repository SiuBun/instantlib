package com.baselib.instant.thread

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.MessageQueue
import com.baselib.instant.manager.IManager
import com.baselib.instant.util.LogUtils
import com.baselib.instant.util.ReflectUtil

/**
 * 线程执行代理类，用于统一管理线程
 *
 * 通过持有{@link #mExecutor} 引用,对所有线程操作方法进行代理
 *
 * @author wsb
 */
class ThreadExecutorProxy :IManager{
    /**
     * 一切交给该管理对象处理
     *
     *
     * 此处持有抽象类，具体操作由实现类去决定
     */
    private val executor: AbstractThreadExecutor

    /**
     * 异步线程对应的处理器
     */
    private var singleAsyncHandler: Handler

    private var mainHandler: Handler

    /**
     * 异步线程,本类中不依靠该成员的getThreadHandler()方法来获取处理对象
     */
    private val mSingleAsyncThread: HandlerThread

    /**
     * 消息队列,可能会被赋值成非主线程的队列
     */
    private var mMsgQueue: MessageQueue? = null

    companion object {
        /**
         * 线程池名称
         */
        private const val POOL_NAME = "proxy_thread_pool"
        /**
         * 默认核心线程数
         */
        private const val DEFAULT_CORE_POOL_SIZE = 2
        /**
         * 默认最大核心线程数
         */
        private const val DEFAULT_MAX_POOL_SIZE = 6

        /**
         * 存活时间,超过会回收
         */
        private const val KEEP_ALIVE_TIME = 60

        /**
         * 异步线程名
         */
        private const val ASYNC_THREAD_NAME = "proxy-single-async-thread"
    }

    init {
        executor = ExtendThreadExecutor(POOL_NAME, DEFAULT_CORE_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE, KEEP_ALIVE_TIME)

        mSingleAsyncThread = HandlerThread(ASYNC_THREAD_NAME)
        mSingleAsyncThread.start()

        singleAsyncHandler = Handler(mSingleAsyncThread.looper)
        mainHandler = Handler(Looper.getMainLooper())

        if (Looper.getMainLooper() == Looper.myLooper()) {
            mMsgQueue = Looper.myQueue()
        } else {
            try {
                mMsgQueue = ReflectUtil.getValue(Looper.getMainLooper(), "mQueue") as MessageQueue?
            } catch (e: Throwable) {
                LogUtils.e("error->", e)
                runOnMainThread(Runnable { mMsgQueue = Looper.myQueue() })
            }

        }
    }

    /**
     * 提交异步任务到线程池中执行
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     * @param priority   线程优先级，该值来自于Thread，不是来自于Process；
     * 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     * Android建议使用OS层级设置优先级，效果更显著
     */
    @JvmOverloads
    fun execute(task: Runnable, threadName: String = "", priority: Int = Thread.NORM_PRIORITY) {
        executor.execute(task, threadName, priority)
    }

    /**
     * 提交一个Runable到异步线程队列，该异步线程为单队列
     *
     * @param r 目标任务
     * @param delay 延迟时长
     */
    @JvmOverloads
    fun runOnAsyncThread(r: Runnable, delay: Long = 0) {
        singleAsyncHandler.postDelayed(r, delay)
    }

    /**
     * 提交一个Runable到主线程队列
     *
     * @param r     目标任务
     * @param delay 延迟时长
     */
    @JvmOverloads
    fun runOnMainThread(r: Runnable, delay: Long = 0) {
        mainHandler.postDelayed(r, delay)
    }


    /**
     * 取消指定的任务
     *
     * @param task
     */
    fun cancel(task: Runnable) {
        executor.cancel(task)
        singleAsyncHandler.removeCallbacks(task)
        mainHandler.removeCallbacks(task)
    }

    /**
     * 提交一个Runnable到主线程空闲时执行
     *
     * @param r 目标任务
     */
    fun runOnIdleTime(r: Runnable) {
        mMsgQueue?.let {
            it.addIdleHandler {
                r.run()
                false
            }
        }
    }

    /**
     * 销毁对象
     */
    private fun destroy() {
        executor.destroy()
        singleAsyncHandler.removeCallbacksAndMessages(null)
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun detach() {
        destroy()
    }
}