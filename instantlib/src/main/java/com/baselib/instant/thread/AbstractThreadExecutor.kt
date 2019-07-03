package com.baselib.instant.thread


/**
 * 抽象的自定义线程执行对象
 *
 *
 * 一切执行线程的方式交给[manager]去处理
 *
 * @author wsb
 */
abstract class AbstractThreadExecutor {

    private var manager: IThreadPoolManager? = null

    private val bytesLock = ByteArray(0)

    /**
     * 子类实现该方法用于初始化自身所持有的线程池管理对象
     *
     *
     * 进行改动扩展的时候只需要改变在该方法内返回新内容即可,不影响原有逻辑
     *
     * @return 线程池对象
     */
    protected abstract fun initThreadPoolManager(): IThreadPoolManager

    /**
     * 执行一个异步任务
     *
     *
     * 如果线程池管理对象未初始化，就先完成初始化再执行
     *
     * @param task 需要执行的任务
     */
    internal fun execute(task: Runnable) {
        if (manager == null) {
            synchronized(bytesLock) {
                manager = initThreadPoolManager()
            }
        }
        manager?.execute(task)
    }

    /**
     * 执行一个异步任务
     *
     * @param task     需要执行的任务
     * @param priority 线程优先级，该值来自于Thread类，不是来自于Process类；
     * 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     * Android建议使用OS层级设置优先级，效果更显著
     */
    internal fun execute(task: Runnable, priority: Int) {
        execute(task, null, priority)
    }

    /**
     * 执行一个异步任务
     *
     * 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
     * Android建议使用OS层级设置优先级，效果更显著
     *
     * @param task       需要执行的任务
     * @param threadName 线程名称
     * @param priority   线程优先级,该值来自于Thread类，不是来自于Process类；
     */
    @JvmOverloads
    internal fun execute(task: Runnable, threadName: String?, priority: Int = Thread.currentThread().priority) {
        val namedTask = NamedTask(task)
        threadName?.let {
            namedTask.threadName = it
        }
        namedTask.priority = priority
        execute(namedTask)
    }

    /**
     * 取消指定的任务
     *
     * @param task
     */
    internal fun cancel(task: Runnable) {
        manager?.cancel(task)
    }

    /**
     * 销毁对象
     */
    internal fun destroy() {
        manager?.cleanUp()
        manager = null
    }


}
