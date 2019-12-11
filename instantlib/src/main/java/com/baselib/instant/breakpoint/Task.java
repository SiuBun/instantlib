package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Task {
    private int mTaskId;
    private String mTaskUrl;
    private String mTaskFileDir;
    private String mTaskFileName;

    private Set<TaskListener> mTaskListenerSet;

    private final byte[] mListenerLock = new byte[0];

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
    }

    public void supplementField(Context context) {
        if (TextUtils.isEmpty(this.mTaskFileName)) {
            this.mTaskFileName = String.valueOf(System.currentTimeMillis());
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


    public Runnable preload(Task.PreloadListener preloadListener) {
        return () -> {
            try {
                String redirectionUrl = DataUtils.getRedirectionUrl(getTaskUrl());
                preloadListener.preloadSuccess(TextUtils.isEmpty(redirectionUrl)?getTaskUrl():redirectionUrl);
            } catch (Exception e) {
                e.printStackTrace();
                preloadListener.preloadFail(e.getMessage());
            }
        };
    }


    public boolean addTaskListener(TaskListener listener) {
        synchronized (mListenerLock){
            return mTaskListenerSet.add(listener);
        }
    }

    public boolean removeTaskListener(TaskListener listener) {
        synchronized (mListenerLock){
            return mTaskListenerSet.remove(listener);
        }
    }

    public void cleanTaskListener(){
        synchronized (mListenerLock){
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

    public void postNewTaskSuccess(int taskId) {
        for (TaskListener listener:mTaskListenerSet){
            listener.postNewTaskSuccess(taskId);
        }
    }

    public void onTaskPreloadFail(String msg) {
        for (TaskListener listener:mTaskListenerSet){
            listener.postNewTaskFail(msg);
        }
    }

    public void onTaskDownloadError(String message) {
        for (TaskListener listener:mTaskListenerSet){
            listener.onTaskDownloadError(message);
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
        void postNewTaskFail(String msg);

        /**
         * 任务添加成功
         *
         * 该任务被添加到任务列表内
         * @param taskId 任务在列表内的id
         * */
        void postNewTaskSuccess(int taskId);

        /**
         * 任务下载过程中出现异常
         *
         * @param message 附带描述
         * */
        void onTaskDownloadError(String message);
    }

    public interface PreloadListener {
        void preloadFail(String message);

        void preloadSuccess(String redirectionUrl);
    }
}
