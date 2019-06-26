package com.baselib.instant.thread;

import android.os.Build;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程管理对象
 *
 * @author wsb
 */
public class ThreadPoolManager implements IThreadPoolManager{

    private final static int DEFAULT_CORE_POOL_SIZE = 4;
    private final static int DEFAULT_MAXIMUM_POOL_SIZE = 4;
    private final static long DEFAULT_KEEP_ALIVE_TIME = 0;
    private final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * 真正执行线程处理的线程池
     */
    private ThreadPoolExecutor mWorkThreadPool;
    /**
     * 保存等待任务的链表队列
     */
    private Queue<Runnable> mWaitTasksQueue;
    /**
     * 任务被拒绝执行的处理器
     */
    private RejectedExecutionHandler mRejectedExecutionHandler = null;

    private final Object mLock = new Object();

    private String mName;

    private ThreadPoolManager() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME,
                DEFAULT_TIME_UNIT, false, null);
    }

    /**
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   存活时间
     * @param unit            时间单位
     * @param isPriority      是否对该线程池的队列内线程都设置为优先
     */
    private ThreadPoolManager(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                              TimeUnit unit, boolean isPriority, final ITaskExecuteListener listener) {
        mWaitTasksQueue = new ConcurrentLinkedQueue<>();
        initRejectedExecutionHandler();
        BlockingQueue<Runnable> queue = isPriority
                ? new PriorityBlockingQueue<>(16)
                : new LinkedBlockingQueue<>(16);

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        };

        if (listener == null) {
            mWorkThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, queue, threadFactory, mRejectedExecutionHandler);
        } else {
            mWorkThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                    unit, queue, threadFactory, mRejectedExecutionHandler) {
                @Override
                protected void beforeExecute(Thread t, Runnable r) {
                    listener.beforeExecute(t, r);
                }

                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    listener.afterExecute(r, t);
                }
            };
        }
    }

    public static ThreadPoolManager buildInstance(String threadPoolManagerName,
                                                  int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        return buildInstance(threadPoolManagerName, corePoolSize, maximumPoolSize, keepAliveTime,
                unit, false);
    }

    public static ThreadPoolManager buildInstance(String threadPoolManagerName,
                                                  int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                                  boolean isPriority) {
        return buildInstance(threadPoolManagerName, corePoolSize, maximumPoolSize, keepAliveTime,
                unit, isPriority, null);

    }

    public static ThreadPoolManager buildInstance(String threadPoolManagerName,
                                                  int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                                  boolean isPriority, ITaskExecuteListener listener) {
        if (threadPoolManagerName == null || "".equals(threadPoolManagerName.trim())
                || corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize
                || keepAliveTime < 0) {
            return null;
        } else {
            ThreadPoolManager threadPoolManager = new ThreadPoolManager(corePoolSize,
                    maximumPoolSize, keepAliveTime, unit, isPriority, listener);
            threadPoolManager.mName = threadPoolManagerName;

            return threadPoolManager;
        }
    }

    /**
     * 获取执行等待队列任务的Runnable
     *
     * @return 执行所有等待队列的任务
     * */
    Runnable getScheduleRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    executeWaitTask();
                }
            }
        };
    }

    private void executeWaitTask() {
        synchronized (mLock) {
            if (hasMoreWaitTask()) {
                Runnable runnable = mWaitTasksQueue.poll();
                if (runnable != null) {
                    execute(runnable);
                }
            }
        }
    }

    /**
     * 初始化任务被拒绝执行的处理器的方法
     */
    private void initRejectedExecutionHandler() {
        mRejectedExecutionHandler = (r, executor) -> {
            // 把被拒绝的任务重新放入到等待队列中
            synchronized (mLock) {
                mWaitTasksQueue.offer(r);
            }
        };
    }

    /**
     * 是否还有等待任务的方法
     *
     * @return
     */
    public boolean hasMoreWaitTask() {
        return !mWaitTasksQueue.isEmpty();
    }

    /**
     * 执行任务的方法
     *
     * @param task
     */
    @Override
    public void execute(Runnable task) {
        if (task != null) {
            mWorkThreadPool.execute(task);
        }
    }

    /**
     * 取消任务
     * <p>
     * 如果等待队列中存在,那么队列移除该任务后再由线程池移除
     *
     * @param task 准备移除的任务
     */
    @Override
    public void cancel(Runnable task) {
        if (task != null) {
            synchronized (mLock) {
                if (mWaitTasksQueue.contains(task)) {
                    mWaitTasksQueue.remove(task);
                }
            }
            mWorkThreadPool.remove(task);
        }
    }

    /**
     * 移除所有的任务
     * <p>
     * 线程池未关闭的情况下，将线程池中的任务都移除掉
     */
    public void removeAllTask() {
        // 如果取task过程中task队列数量改变了会抛异常
        try {
            if (!mWorkThreadPool.isShutdown()) {
                BlockingQueue<Runnable> tasks = mWorkThreadPool.getQueue();
                for (Runnable task : tasks) {
                    mWorkThreadPool.remove(task);
                }
            }
        } catch (Throwable e) {
            Log.e("ThreadPoolManager", "removeAllTask " + e.getMessage());
        }
    }

    public boolean isShutdown() {
        return mWorkThreadPool.isShutdown();
    }

    /**
     * 清理方法
     * <p>
     * 步骤
     * <p>
     * 1.关闭线程池
     * 2.置空被拒处理器
     * 3.清空队列
     */
    @Override
    public void cleanUp() {
        if (!mWorkThreadPool.isShutdown()) {
            try {
                mWorkThreadPool.shutdownNow();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        mRejectedExecutionHandler = null;
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
        synchronized (mLock) {
            mWaitTasksQueue.clear();
        }
    }

    public void setThreadFactory(ThreadFactory factory) {
        mWorkThreadPool.setThreadFactory(factory);
    }

    public void allowCoreThreadTimeOut(boolean allow) {
        if (Build.VERSION.SDK_INT > 8) {
            mWorkThreadPool.allowCoreThreadTimeOut(allow);
        }
    }

    public String getManagerName() {
        return mName;
    }


    /**
     * 线程任务执行监听
     */
    public interface ITaskExecuteListener {
        /**
         * 同步{@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}回调
         */
        void beforeExecute(Thread thread, Runnable task);

        /**
         * 同步{@link ThreadPoolExecutor#afterExecute(Thread, Runnable)}回调
         */
        void afterExecute(Runnable task, Throwable throwable);
    }
}
