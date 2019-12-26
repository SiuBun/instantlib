package com.baselib.instant.breakpoint;

import android.content.Context;
import android.support.annotation.NonNull;

import com.baselib.instant.breakpoint.bussiness.BreakPointDownloader;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.util.DataCheck;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 断点下载工具类,对外暴露方法
 *
 * @author wsb
 */
public class BreakPointHelper {

    /**
     * 保存执行任务的容器
     */
    private final HashMap<Integer, Task> mTaskMap = new HashMap<>(BreakPointConst.DEFAULT_CAPACITY);

    /**
     * 负责下载功能
     */
    private final BreakPointDownloader mBreakPointDownloader = new BreakPointDownloader();

    /**
     * 任务锁
     */
    private final byte[] mTaskLock = new byte[0];

    private BreakPointHelper() {

    }

    public static BreakPointHelper getInstance() {
        return Provider.get();
    }

    /**
     * 初始化断点下载(必调)
     * <p>
     * 加载原来保存的下载任务到队列内并添加数据库监听
     *
     * @param context 上下文
     */
    public void attachApplication(@NonNull Context context) {
        mBreakPointDownloader.loadTaskRecord(context, taskMap -> {

            for (Map.Entry<Integer, Task> entry : taskMap.entrySet()) {
                final Task task = entry.getValue();
                task.addTaskListener(getDatabaseTaskListener(task));
                mTaskMap.put(entry.getKey(), task);
            }
        });
    }

    /**
     * 解除断点下载相关资源
     * <p>
     * 1.移除任务监听
     * <p>
     * 2.清空列表
     */
    public void detachApplication() {
        for (Map.Entry<Integer, Task> taskEntry : mTaskMap.entrySet()) {
            taskEntry.getValue().cleanTaskListener();
            taskEntry.getValue().onAppDetach();
        }
        mTaskMap.clear();
    }

    /**
     * 全列表下载开始
     * <p>
     * 只开始下载那些未完成的任务
     *
     * @param context 上下文
     */
    public void commitTask(Context context) {
        for (Map.Entry<Integer, Task> entry : mTaskMap.entrySet()) {
            final Task task = entry.getValue();
            if (task.incompleteState()) {
                postTask(context, task);
            }
        }
    }


    /**
     * 提交任务
     * <p>
     * 如果列表中未存在重复任务(判断标准查看{@link #taskDuplicate(Task)}),则任务添加到列表中.列表中已存在,说明当前或之前应用生命周期内出现过该任务的提交,
     * 任务监听将会挂载到之前已创建的任务上
     *
     * @param context 上下文
     * @param task    任务对象
     */
    public void postTask(@NonNull Context context, @NonNull Task task) {
        synchronized (mTaskLock) {
            AtomicReference<Task> t = new AtomicReference<>();


            if (addTask(context, task)) {
                LogUtils.i("新任务添加成功");
                task.addTaskListener(getDatabaseTaskListener(task));
                task.postNewTaskSuccess();
                t.set(task);
            } else {
                LogUtils.i("关联已有任务");
                DataCheck.checkNoNullWithCallback(mTaskMap.get(task.getTaskId()), taskBeListen -> {
                    taskBeListen.addTaskListeners(task.getTaskListener());
                    t.set(taskBeListen);
                });
            }

            LogUtils.i("最终执行的任务" + t.get());
            mBreakPointDownloader.executeTask(context, t.get());

        }
    }

    @NotNull
    private TaskPostListener getDatabaseTaskListener(Task t) {
        return new TaskPostListener() {

            @Override
            public void postNewTaskSuccess(int taskId) {
                LogUtils.d("数据库插入新条目,id为" + taskId);
                mBreakPointDownloader.onNewTaskAdd(t);
            }

            @Override
            public void onTaskDownloadError(String message) {
                mBreakPointDownloader.onTaskDownloadError(t);
            }

            @Override
            public void onTaskProgressUpdate(long taskTotalSize, long length) {
                mBreakPointDownloader.onTaskProgressUpdateById(t);
            }

            @Override
            public void onTaskDownloadFinish() {

            }

            @Override
            public void onTaskCancel() {
                mBreakPointDownloader.deleteTaskRecord(t);
            }

            @Override
            public void onTaskDownloadStart(String downloadUrl) {

            }
        };
    }


    /**
     * 添加任务
     * <p>
     * 如果列表中未存在重复任务(判断标准查看{@link #taskDuplicate(Task)}),则任务添加到列表中.列表中已存在,说明当前或之前应用生命周期内出现过该任务的提交,
     * 故任务监听会挂载到之前已创建的任务上
     * <p>
     * 和{@link #removeTask(int)}相对应
     *
     * @param context 上下文
     * @param task    任务对象
     * @return true 代表操作成功
     */
    public boolean addTask(@NonNull Context context, @NonNull Task task) {
        synchronized (mTaskLock) {
            boolean addResult = false;
            task.supplementField(context);
            LogUtils.i("所准备添加的任务内容如下" + task);
            if (!taskDuplicate(task)) {
                mTaskMap.put(task.getTaskId(), task);
                addResult = true;
            }
            return addResult;
        }
    }

    /**
     * 根据任务id从列表中移除
     * <p>
     * 和{@link #postTask(Context, Task)}相对应
     *
     * @param taskId 任务id
     * @return true 代表操作成功
     */
    public boolean removeTask(int taskId) {
        synchronized (mTaskLock) {
            AtomicBoolean removeResult = new AtomicBoolean(false);
            DataCheck.checkNoNullWithCallback(mTaskMap.get(taskId), task -> {
                mBreakPointDownloader.mainThreadExecute(() -> {
                    task.onTaskCancel();
                    task.cleanTaskListener();
                    removeResult.set(mTaskMap.remove(taskId) != null);
                });
            });
            return removeResult.get();
        }
    }

    /**
     * 根据任务id移除该任务的监听对象
     * <p>
     * 和{@link #addTaskListener(int, TaskPostListener)}相对应
     *
     * @param taskId   任务id
     * @param listener 任务的监听对象
     * @return true 代表操作成功
     */
    public boolean removeTaskListener(int taskId, @NonNull TaskPostListener listener) {
        AtomicBoolean removeResult = new AtomicBoolean(false);
        DataCheck.checkNoNullWithCallback(mTaskMap.get(taskId), task -> removeResult.set(task.removeTaskListener(listener)));
        return removeResult.get();
    }

    /**
     * 根据任务id添加该任务的监听对象
     * <p>
     * 和{@link #removeTaskListener(int, TaskPostListener)}相对应
     *
     * @param taskId   任务id
     * @param listener 任务的监听对象
     * @return true 代表操作成功
     */
    public boolean addTaskListener(int taskId, @NonNull TaskPostListener listener) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        DataCheck.checkNoNullWithCallback(mTaskMap.get(taskId), task -> addResult.set(task.addTaskListener(listener)));
        return addResult.get();
    }

    private boolean taskDuplicate(Task task) {
        return mTaskMap.containsKey(task.getTaskId());
    }

    private static class Provider {
        private static final BreakPointHelper DOWNLOADER = new BreakPointHelper();

        static BreakPointHelper get() {
            return DOWNLOADER;
        }
    }

}
