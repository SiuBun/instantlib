package com.baselib.instant.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;

import com.baselib.instant.util.LogUtils;
import com.baselib.instant.util.ReflectUtil;
import com.baselib.manager.IManager;

/**
 * 线程执行代理类，用于统一管理线程
 * <p>
 * 通过持有{@link #mExecutor} 引用,对所有线程操作方法进行代理
 *
 * @author wsb
 */
public class ThreadExecutorProxy implements IManager {
    /**
     * 线程池名称
     */
    private final static String POOL_NAME = "proxy_thread_pool";

    /**
     * 默认核心线程数
     */
    private final static int DEFAULT_CORE_POOL_SIZE = 2;
    /**
     * 默认最大核心线程数
     */
    private final static int DEFAULT_MAX_POOL_SIZE = 6;

    /**
     * 存活时间,超过会回收
     */
    private final static int KEEP_ALIVE_TIME = 60;

    /**
     * 异步线程名
     */
    private final static String ASYNC_THREAD_NAME = "proxy-single-async-thread";

    /**
     * 一切交给该管理对象处理
     * <p>
     * 此处持有抽象类，具体操作由实现类去决定
     */
    private AbstractThreadExecutor mExecutor;


    /**
     * 异步线程,本类中不依靠该成员的getThreadHandler()方法来获取处理对象
     */
    private HandlerThread mSingleAsyncThread;

    /**
     * 异步线程对应的处理器
     */
    private Handler mSingleAsyncHandler;

    private Handler mMainHandler;

    /**
     * 消息队列,可能会被赋值成非主线程的队列
     */
    private MessageQueue mMsgQueue;

    public ThreadExecutorProxy() {
        mExecutor = new ExtendThreadExecutor(POOL_NAME, DEFAULT_CORE_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE, KEEP_ALIVE_TIME);

        mSingleAsyncThread = new HandlerThread(ASYNC_THREAD_NAME);
        mSingleAsyncThread.start();
        mSingleAsyncHandler = new Handler(mSingleAsyncThread.getLooper());

        mMainHandler = new Handler(Looper.getMainLooper());

        //兼容处理非主线程构建的情况
        if (Looper.getMainLooper() == Looper.myLooper()) {
            mMsgQueue = Looper.myQueue();
        } else {
            Object queue = null;
            try {
                queue = ReflectUtil.getValue(Looper.getMainLooper(), "mQueue");
            } catch (Throwable e) {
                LogUtils.e("error->", e);
            }
            if (queue instanceof MessageQueue) {
                mMsgQueue = (MessageQueue) queue;
            } else {
                runOnMainThread(() -> mMsgQueue = Looper.myQueue());
            }
        }
    }

    /**
     * 提交异步任务到线程池中执行
     *
     * @param task 需要执行的任务
     */
    public void execute(Runnable task) {
        mExecutor.execute(task);
    }

    /**
     * 提交异步任务到线程池中执行
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     */
    public void execute(Runnable task, String threadName) {
        mExecutor.execute(task, threadName);
    }

    /**
     * 提交异步任务到线程池中执行
     *
     * @param task     需要执行的任务
     * @param priority 线程优先级，该值来自于Thread，不是来自于Process；
     *                 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     *                 Android建议使用OS层级设置优先级，效果更显著
     */
    public void execute(Runnable task, int priority) {
        mExecutor.execute(task, priority);
    }

    /**
     * 提交异步任务到线程池中执行
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     * @param priority   线程优先级，该值来自于Thread，不是来自于Process；
     *                   如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     *                   Android建议使用OS层级设置优先级，效果更显著
     */
    public void execute(Runnable task, String threadName, int priority) {
        mExecutor.execute(task, threadName, priority);
    }

    /**
     * 取消指定的任务
     *
     * @param task
     */
    public void cancel(final Runnable task) {
        mExecutor.cancel(task);
        mSingleAsyncHandler.removeCallbacks(task);
        mMainHandler.removeCallbacks(task);
    }

    /**
     * 销毁对象
     */
    public void destroy() {
        mExecutor.destroy();
        mSingleAsyncHandler.removeCallbacksAndMessages(null);
        mMainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 提交一个Runable到异步线程队列，该异步线程为单队列
     *
     * @param r
     */
    public void runOnAsyncThread(Runnable r) {
        mSingleAsyncHandler.post(r);
    }

    /**
     * 提交一个Runable到异步线程队列，该异步线程为单队列
     *
     * @param r 目标任务
     * @param delay 延迟时长
     */
    public void runOnAsyncThread(Runnable r, long delay) {
        mSingleAsyncHandler.postDelayed(r, delay);
    }

    /**
     * 提交一个Runable到主线程队列
     *
     * @param r 目标任务
     */
    public void runOnMainThread(Runnable r) {
        mMainHandler.post(r);
    }

    /**
     * 提交一个Runable到主线程队列
     *
     * @param r     目标任务
     * @param delay 延迟时长
     */
    public void runOnMainThread(Runnable r, long delay) {
        mMainHandler.postDelayed(r, delay);
    }

    /**
     * 提交一个Runnable到主线程空闲时执行
     *
     * @param r 目标任务
     */
    public void runOnIdleTime(final Runnable r) {
        IdleHandler handler = () -> {
            r.run();
            return false;
        };
        mMsgQueue.addIdleHandler(handler);
    }


    @Override
    public void detach() {
        destroy();
    }
}
