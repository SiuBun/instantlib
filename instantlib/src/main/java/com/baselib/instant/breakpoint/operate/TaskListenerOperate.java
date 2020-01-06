package com.baselib.instant.breakpoint.operate;

import com.baselib.instant.breakpoint.TaskPostListener;

import java.util.Collection;

/**
 * 对任务下载监听对象的操作规范
 *
 * @author wsb
 */
public interface TaskListenerOperate {
    /**
     * 添加任务下载监听对象
     *
     * @param listener 任务下载监听对象
     * @return 添加结果
     */
    boolean addTaskListener(TaskPostListener listener);

    /**
     * 移除任务下载监听对象
     *
     * @param listener 任务下载监听对象
     * @return 添加结果
     */
    boolean removeTaskListener(TaskPostListener listener);

    /**
     * 清空任务下载监听对象
     */
    void cleanTaskListener();

    /**
     * 获取任务下载监听对象队列
     *
     * @return 对任务的所有监听对象
     */
    Collection<TaskPostListener> getTaskListener();

    /**
     * 批量添加任务下载监听对象
     *
     * @param listeners 批量任务下载监听对象
     */
    void addTaskListeners(Collection<TaskPostListener> listeners);
}
