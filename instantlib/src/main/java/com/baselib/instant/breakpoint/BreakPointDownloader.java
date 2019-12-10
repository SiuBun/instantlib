package com.baselib.instant.breakpoint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baselib.instant.breakpoint.bussiness.DownloadExecutor;
import com.baselib.instant.breakpoint.database.DataBaseRepository;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataCheck;
import com.baselib.instant.util.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 断点下载工具类
 *
 * @author wsb
 */
public class BreakPointDownloader {
    /**
     * 下载任务本地存储对象
     */
    private DataBaseRepository mDatabaseRepository;

    private final HashMap<Integer, Task> mTaskMap = new HashMap<>(BreakPointConst.DEFAULT_CAPACITY);

    private final DownloadExecutor mExecutor = new DownloadExecutor();

    private final byte[] mTaskLock = new byte[0];

    private BreakPointDownloader() {

    }

    public static BreakPointDownloader getInstance() {
        return Provider.get();
    }

    /**
     * 初始化断点下载
     * <p>
     * 加载原来保存的下载任务到队列内
     *
     * @param context 上下文
     */
    public void attachApplication(@NonNull Context context) {
        final Context applicationContext = context.getApplicationContext();

        mDatabaseRepository = new DataBaseRepository(applicationContext);
        asyncExecute(() -> {
            final List<TaskRecordEntity> taskRecords = mDatabaseRepository.loadAllTaskRecord();
            DataCheck.checkEmpty(taskRecords);

            DataCheck.checkNoEmptyWithCallback(taskRecords, data -> {
                for (TaskRecordEntity recordEntity : data) {
                    final Task task = Task.Builder.transformRecord(recordEntity);
                    mTaskMap.put(task.getTaskId(), task);
                }
            });

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
        for (Map.Entry<Integer, Task> task : mTaskMap.entrySet()) {
            task.getValue().cleanTaskListener();
            task.getValue().onAppDetach();
        }
        mTaskMap.clear();
    }

    private void asyncExecute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    /**
     * 提交任务
     * <p>
     * 如果列表中未存在重复任务(判断标准查看{@link #taskDuplicate(Task)}),则任务添加到列表中.列表中已存在,说明当前或之前应用生命周期内出现过该任务的提交,
     * 故任务监听会挂载到之前已创建的任务上
     *
     * @param context 上下文
     * @param task    任务对象
     */
    public void postTask(@NonNull Context context, @NonNull Task task) {
        synchronized (mTaskLock) {
            if (addTask(context, task)) {
                LogUtils.i("任务添加成功");
                task.postNewTaskSuccess(task.getTaskId());
            } else {
                task.postNewTaskFail("当前或之前应用生命周期内出现过该任务的提交,故任务监听会挂载到之前已创建的任务上");
                DataCheck.checkNoNullWithCallback(mTaskMap.get(task.getTaskId()), taskBeListen -> taskBeListen.addTaskListeners(task.getTaskListerer()));
            }

            executeTask(context, task);
        }
    }

    private void executeTask(Context context, Task task) {

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
                task.cleanTaskListener();
                removeResult.set(mTaskMap.remove(taskId) != null);
            });
            return removeResult.get();
        }
    }

    /**
     * 根据任务id移除该任务的监听对象
     * <p>
     * 和{@link #addTaskListener(int, Task.TaskListener)}相对应
     *
     * @param taskId   任务id
     * @param listener 任务的监听对象
     * @return true 代表操作成功
     */
    public boolean removeTaskListener(int taskId, @NonNull Task.TaskListener listener) {
        AtomicBoolean removeResult = new AtomicBoolean(false);
        DataCheck.checkNoNullWithCallback(mTaskMap.get(taskId), task -> removeResult.set(task.removeTaskListener(listener)));
        return removeResult.get();
    }

    /**
     * 根据任务id添加该任务的监听对象
     * <p>
     * 和{@link #removeTaskListener(int, Task.TaskListener)}相对应
     *
     * @param taskId   任务id
     * @param listener 任务的监听对象
     * @return true 代表操作成功
     */
    public boolean addTaskListener(int taskId, @NonNull Task.TaskListener listener) {
        AtomicBoolean addResult = new AtomicBoolean(false);
        DataCheck.checkNoNullWithCallback(mTaskMap.get(taskId), task -> addResult.set(task.addTaskListener(listener)));
        return addResult.get();
    }

    private boolean taskDuplicate(Task task) {
        return mTaskMap.containsKey(task.getTaskId());
    }

    private static class Provider {
        private static final BreakPointDownloader DOWNLOADER = new BreakPointDownloader();

        static BreakPointDownloader get() {
            return DOWNLOADER;
        }
    }


}
