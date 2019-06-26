package com.baselib.instant.thread;


/**
 * 抽象的自定义线程执行对象
 * <p>
 * 一切执行线程的方式交给{@link #mManager}去处理
 *
 * @author wsb
 */
public abstract class AbstractThreadExecutor {

    protected IThreadPoolManager mManager;

    private final byte[] mLock = new byte[0];

    /**
     * 子类实现该方法用于初始化自身所持有的线程池管理对象
     * <p>
     * 进行改动扩展的时候只需要改变在该方法内返回新内容即可,不影响原有逻辑
     *
     * @return 线程池对象
     */
    protected abstract IThreadPoolManager initThreadPoolManager();

    /**
     * 执行一个异步任务
     * <p>
     * 如果线程池管理对象未初始化，就先完成初始化再执行
     *
     * @param task 需要执行的任务
     */
    void execute(Runnable task) {
        if (mManager == null) {
            synchronized (mLock) {
                if (mManager == null) {
                    mManager = initThreadPoolManager();
                }
            }
        }
        mManager.execute(task);
    }

    /**
     * 执行一个异步任务
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     */
    void execute(Runnable task, String threadName) {
        execute(task, threadName, Thread.currentThread().getPriority());
    }

    /**
     * 执行一个异步任务
     *
     * @param task     需要执行的任务
     * @param priority 线程优先级，该值来自于{@link Thread}类，不是来自于{@link Process}类；
     *                 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     *                 Android建议使用OS层级设置优先级，效果更显著
     */
    void execute(Runnable task, int priority) {
        execute(task, null, priority);
    }

    /**
     * 执行一个异步任务
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     * @param priority   线程优先级,该值来自于{@link Thread}类，不是来自于{@link Process}类；
     *                   如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     *                   Android建议使用OS层级设置优先级，效果更显著
     */
    void execute(Runnable task, String threadName, int priority) {
        NamedTask namedTask = new NamedTask(task);
        namedTask.mThreadName = threadName;
        namedTask.mPriority = priority;
        execute(namedTask);
    }

    /**
     * 取消指定的任务
     *
     * @param task
     */
    void cancel(final Runnable task) {
        if (mManager != null) {
            mManager.cancel(task);
        }
    }

    /**
     * 销毁对象
     */
    void destroy() {
        if (mManager != null) {
            mManager.cleanUp();
            mManager = null;
        }
    }


}
