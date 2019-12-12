package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Task {
    private int mTaskId;
    private String mTaskUrl;
    private String mTaskFileDir;
    private String mTaskFileName;

    private Set<TaskListener> mTaskListenerSet;

    private final byte[] mListenerLock = new byte[0];

    private File mTmpAccessFile;

    private File[] mCacheFiles;

    private final CountDownLatch mCountDownLatch;

    public int getTaskId() {
        return mTaskId;
    }

    public String getTaskUrl() {
        return mTaskUrl;
    }

    public String getTaskFileDir() {
        return mTaskFileDir;
    }

    public String getTaskFileName() {
        return mTaskFileName;
    }

    public String getTaskPath() {
        return getTaskFileDir() + File.separator + getTaskFileName();
    }

    @Override
    public String toString() {
        return "Task{" +
                "mTaskId=" + mTaskId +
                ", mTaskUrl='" + mTaskUrl + '\'' +
                ", mTaskFileDir='" + mTaskFileDir + '\'' +
                ", mTaskFileName='" + mTaskFileName + '\'' +
                ", mTaskListenerSet='" + mTaskListenerSet + '\'' +
                '}';
    }

    private Task() {
        mTaskListenerSet = new HashSet<>();
        mCacheFiles = new File[BreakPointConst.DEFAULT_THREAD_COUNT];
        mCountDownLatch = new CountDownLatch(BreakPointConst.DEFAULT_THREAD_COUNT);
    }

    public File getTmpAccessFile() {
        return mTmpAccessFile;
    }

    public CountDownLatch getCountDownLatch() {
        return mCountDownLatch;
    }

    public void supplementField(Context context) {
        if (TextUtils.isEmpty(this.mTaskFileName)) {
            this.mTaskFileName = getFileName(getTaskUrl());
        }

        if (TextUtils.isEmpty(this.mTaskFileDir)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                this.mTaskFileDir = DataUtils.spliceTaskFileDir(Environment.getExternalStorageDirectory());
            } else {
                this.mTaskFileDir = DataUtils.spliceTaskFileDir(context.getFilesDir());
            }
            LogUtils.i("参数调整以后的文件目录为" + this.mTaskFileDir);
        }

//        任务的唯一标识,根据下载url和保存位置来生成
        this.mTaskId = DataUtils.generateId(getTaskUrl(), getTaskPath());
    }

    // 获取下载文件的名称
    public String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 该方法作为下载任务的前置任务,需要在子线程里执行,避免阻塞UI线程
     *
     * @param preloadListener 前置任务加载监听
     */
    public Runnable preload(Task.PreloadListener preloadListener) {
        return () -> {
            try {
                String redirectionUrl = DataUtils.getRedirectionUrl(getTaskUrl());
                preloadListener.preloadSuccess(TextUtils.isEmpty(redirectionUrl) ? getTaskUrl() : redirectionUrl);
            } catch (Exception e) {
                e.printStackTrace();
                preloadListener.preloadFail(e.getMessage());
            }
        };
    }


    public boolean addTaskListener(TaskListener listener) {
        synchronized (mListenerLock) {
            return mTaskListenerSet.add(listener);
        }
    }

    public boolean removeTaskListener(TaskListener listener) {
        synchronized (mListenerLock) {
            return mTaskListenerSet.remove(listener);
        }
    }

    public void cleanTaskListener() {
        synchronized (mListenerLock) {
            mTaskListenerSet.clear();
        }
    }

    public void onAppDetach() {

    }

    public Set<TaskListener> getTaskListener() {
        return this.mTaskListenerSet;
    }

    public void addTaskListeners(Set<TaskListener> listeners) {
        mTaskListenerSet.addAll(listeners);
    }

    public void postNewTaskSuccess() {
        for (TaskListener listener : mTaskListenerSet) {
            listener.postNewTaskSuccess(getTaskId());
        }
    }

    public void onTaskPreloadFail(String msg) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.postTaskFail(msg);
        }
    }

    public void onTaskDownloadError(String message) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskDownloadError(message);
        }
    }

    public void setTaskTmpFile(File tmpFile) {
        this.mTmpAccessFile = tmpFile;
    }

    public void setCacheFile(int threadId, File cacheFile) {
        mCacheFiles[threadId] = cacheFile;
    }

    public void requestDownloadFinish() {
        mTmpAccessFile.renameTo(new File(getTaskFileDir(), getTaskFileName()));

        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskDownloadFinish();
        }
    }

    public void onTaskProgressUpdate(int threadId, long progress) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskProgressUpdate(threadId, progress);
        }
    }


    public static class Builder {
        private String mTaskUrl;
        private String mTaskFileDir;
        private String mTaskFileName;

        public static Task transformRecord(TaskRecordEntity recordEntity) {
            return new Builder()
                    .setTaskUrl(recordEntity.getUrl())
                    .setTaskFileDir(recordEntity.getFileDir())
                    .setTaskFileName(recordEntity.getFileName()).build();
        }

        public Builder setTaskFileDir(@NonNull String fileDir) {
            this.mTaskFileDir = fileDir;
            return this;
        }

        public Builder setTaskUrl(@NonNull String taskUrl) {
            if (TextUtils.isEmpty(taskUrl)) {
                throw new IllegalArgumentException("下载链接不能为空字符串或null对象");
            }
            this.mTaskUrl = taskUrl;
            return this;
        }

        public Builder setTaskFileName(@NonNull String fileName) {
            this.mTaskFileName = fileName;
            return this;
        }

        public Task build() {
            final Task task = new Task();
            task.mTaskUrl = this.mTaskUrl;
            task.mTaskFileDir = this.mTaskFileDir;
            task.mTaskFileName = this.mTaskFileName;
            return task;
        }
    }

    public interface TaskListener {
        /**
         * 任务添加失败
         * <p>
         * 提交失败可能包括如下常见原因:
         * <p>
         * 当前或之前的应用生命周期内,已完成对该任务的添加
         *
         * @param msg 附带描述
         */
        void postTaskFail(String msg);

        /**
         * 任务添加成功
         * <p>
         * 该任务被添加到任务列表内
         *
         * @param taskId 任务在列表内的id
         */
        void postNewTaskSuccess(int taskId);

        /**
         * 任务下载过程中出现异常
         *
         * @param message 附带描述
         */
        void onTaskDownloadError(String message);

        void onTaskProgressUpdate(int threadId, long progress);

        void onTaskDownloadFinish();
    }

    public interface PreloadListener {
        void preloadFail(String message);

        void preloadSuccess(String redirectionUrl);
    }
}
