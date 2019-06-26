package com.baselib.instant.thread;

import java.util.concurrent.TimeUnit;

/**
 * 扩展的线程执行对象
 *
 * @author wsb
 */
class ExtendThreadExecutor extends AbstractThreadExecutor {

    private final String mPoolName;
    private final int mCorePoolSize;
    private final int mDefaultMaxPoolSize;
    private final int mKeepAliveTime;

    ExtendThreadExecutor(String poolName, int corePoolSize, int defaultMaxPoolSize, int keepAliveTime) {
        this.mPoolName = poolName;
        this.mCorePoolSize = corePoolSize > defaultMaxPoolSize ? defaultMaxPoolSize : corePoolSize;
        this.mDefaultMaxPoolSize = defaultMaxPoolSize;
        this.mKeepAliveTime = keepAliveTime;
    }

    @Override
    protected IThreadPoolManager initThreadPoolManager() {
        ThreadPoolManager manager = ThreadPoolManager.buildInstance(mPoolName, mCorePoolSize,
                mDefaultMaxPoolSize, mKeepAliveTime, TimeUnit.SECONDS, false,
                getTaskExecuteListener());
        manager.allowCoreThreadTimeOut(true);
        return manager;
    }

    private ThreadPoolManager.ITaskExecuteListener getTaskExecuteListener() {
        return new ThreadPoolManager.ITaskExecuteListener() {

            @Override
            public void beforeExecute(Thread thread, Runnable task) {
                if (task instanceof NamedTask) {
                    NamedTask namedTask = (NamedTask) task;
                    if (namedTask.mThreadName != null) {
                        thread.setName(namedTask.mThreadName);
                    }
                    thread.setPriority(namedTask.mPriority);
                }
            }

            @Override
            public void afterExecute(Runnable task, Throwable throwable) {

            }

        };
    }
}