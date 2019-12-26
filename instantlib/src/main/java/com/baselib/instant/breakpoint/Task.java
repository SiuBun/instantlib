package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.operate.PreloadListener;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 下载的任务对象
 *
 * @author wsb
 */
public class Task {

    /**
     * 任务的唯一标识,补齐之后生成
     */
    private int mTaskId;

    /**
     * 文件预期下载地址
     */
    private String mTaskUrl;

    /**
     * 文件保存目录
     */
    private String mTaskFileDir;

    /**
     * 文件保存名称
     */
    private String mTaskFileName;

    /**
     * 任务监听列表
     */
    private Set<TaskPostListener> mTaskListenerSet;

    /**
     * 监听对象锁
     */
    private final byte[] mLock = new byte[0];

    /**
     * 下载的文件,下载完成以前以占位文件形式保存,下载完成后修改文件类型
     */
    private File mTmpAccessFile;

    /**
     * 各任务已下载长度
     */
    private Map<String, Long> mDownloadFileCache;

    private final CountDownLatch mCountDownLatch;

    /**
     * 任务状态
     */
    private int mTaskState;

    /**
     * 文件总大小
     */
    private Long mTaskTotalSize;

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
        mTaskState = BreakPointConst.DOWNLOAD_WAITING;
        mTaskListenerSet = new HashSet<>();
        mDownloadFileCache = new HashMap<>(BreakPointConst.DEFAULT_THREAD_COUNT);
        mCountDownLatch = new CountDownLatch(BreakPointConst.DEFAULT_THREAD_COUNT);
    }

    public File getTmpAccessFile() {
        return mTmpAccessFile;
    }

    public CountDownLatch getCountDownLatch() {
        return mCountDownLatch;
    }

    /**
     * 补齐成员
     * <p>
     * 对该下载任务的保存文件名,保存位置及id进行生成
     *
     * @param context 上下文
     */
    void supplementField(Context context) {
        if (TextUtils.isEmpty(this.mTaskFileName)) {
            this.mTaskFileName = getFileNameByUrl(getTaskUrl());
        }

        if (TextUtils.isEmpty(this.mTaskFileDir)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                this.mTaskFileDir = DataUtils.spliceTaskFileDir(Environment.getExternalStorageDirectory());
            } else {
                this.mTaskFileDir = DataUtils.spliceTaskFileDir(context.getFilesDir());
            }
            LogUtils.i("参数调整以后的文件目录为" + this.mTaskFileDir);
        }

        if (0 == this.mTaskId) {
            LogUtils.i("根据下载url和保存位置来生成任务的唯一标识");
            setTaskId(DataUtils.generateId(getTaskUrl(), getTaskPath()));
        }
    }

    /**
     * 从url链接获取下载文件的名称
     */
    private String getFileNameByUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 该方法作为下载任务的前置任务,需要在子线程里执行,避免阻塞UI线程
     *
     * @param preloadListener 前置任务加载监听
     */
    @WorkerThread
    public Runnable preload(PreloadListener preloadListener) {
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


    public boolean addTaskListener(TaskPostListener listener) {
        synchronized (mLock) {
            return mTaskListenerSet.add(listener);
        }
    }

    boolean removeTaskListener(TaskPostListener listener) {
        synchronized (mLock) {
            return mTaskListenerSet.remove(listener);
        }
    }

    void cleanTaskListener() {
        synchronized (mLock) {
            mTaskListenerSet.clear();
        }
    }

    public void onAppDetach() {

    }

    public Set<TaskPostListener> getTaskListener() {
        return this.mTaskListenerSet;
    }

    public void addTaskListeners(Set<TaskPostListener> listeners) {
        mTaskListenerSet.addAll(listeners);
    }

    /**
     * 提交任务成功通知所有监听对象
     */
    public void postNewTaskSuccess() {
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.postNewTaskSuccess(getTaskId());
        }
    }

    /**
     * 预加载成功通知所有监听对象
     *
     * @param realDownloadUrl 最终下载的链接
     */
    public void onTaskPreloadSuccess(String realDownloadUrl) {
        setTaskState(BreakPointConst.DOWNLOAD_PREPARED);
    }

    /**
     * 预加载失败通知所有监听对象
     *
     * @param msg 附带消息
     */
    public void onTaskPreloadFail(String msg) {
        setTaskState(BreakPointConst.DOWNLOAD_ERROR);
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.onTaskDownloadError(msg);
        }
    }

    /**
     * 任务取消通知所有监听对象
     */
    public void onTaskCancel() {
        setTaskState(BreakPointConst.DOWNLOAD_CANCEL);
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.onTaskCancel();
        }
    }

    /**
     * 任务下载过程出错,通知所有监听对象
     *
     * @param message 附带消息
     */
    public void onTaskDownloadError(String message) {
        setTaskState(BreakPointConst.DOWNLOAD_ERROR);
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.onTaskDownloadError(message);
        }
    }

    /**
     * 下载进度更新通知所有监听对象
     *
     * @param length 已下载的文件长度
     */
    public void onTaskProgressUpdate(long length) {
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.onTaskProgressUpdate(getTaskTotalSize(), length);
        }
    }

    /**
     * 下载开始通知所有监听对象
     *
     * @param downloadUrl 从该链接处进行真正的文件下载
     */
    public void onTaskDownloadStart(String downloadUrl) {
        setTaskState(BreakPointConst.DOWNLOAD_START);
        for (TaskPostListener listener : mTaskListenerSet) {
            listener.onTaskDownloadStart(downloadUrl);
        }
    }

    /**
     * 当分段文件进度更新的时候调用
     * <p>
     * 将当前所有分段文件长度累加得到总的下载长度,再返回给客户端,因为客户端只关注整体任务下载
     *
     * @param threadId             线程id
     * @param threadDownloadLength 分段任务已下载长度
     */
    public void onRangeFileProgressUpdate(int threadId, long threadDownloadLength) {
        changeDownloadCacheById(threadId, threadDownloadLength);
        synchronized (mLock) {
            long totalLength = 0;
            for (Map.Entry<String, Long> entry : mDownloadFileCache.entrySet()) {
                totalLength += entry.getValue();
            }
            onTaskProgressUpdate(totalLength);
        }
    }


    private void requestDownloadSuccess() {
        setTaskState(BreakPointConst.DOWNLOAD_SUCCESS);

        if (mTmpAccessFile.renameTo(new File(getTaskFileDir(), getTaskFileName()))) {
            for (TaskPostListener listener : mTaskListenerSet) {
                listener.onTaskDownloadFinish();
            }
        }else {
            LogUtils.e("文件下载完成时修改名称失败");
        }
    }

    public TaskRecordEntity parseToRecord() {
        final TaskRecordEntity recordEntity = new TaskRecordEntity();
        recordEntity.setCurrentSize(getTaskCurrentSizeJson());
        recordEntity.setFileDir(getTaskFileDir());
        recordEntity.setFileName(getTaskFileName());
        recordEntity.setId(getTaskId());
        recordEntity.setState(getTaskState());
        recordEntity.setTotalSize(getTaskTotalSize());
        recordEntity.setUpdateTime(System.currentTimeMillis());
        recordEntity.setUrl(getTaskUrl());
        return recordEntity;
    }

    public String getTaskCurrentSizeJson() {
        final JSONObject jsonObject = new JSONObject();
        synchronized (mLock) {
            try {
                for (Map.Entry<String, Long> entry : mDownloadFileCache.entrySet()) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    /**
     * 创建占位文件并设置长度
     *
     * @param contentLength 文件总长度
     */
    private void createTaskTmpFile(long contentLength) throws Exception {
        setTaskTotalSize(contentLength);
        final File tmpFile = new File(getTaskFileDir(), getTaskFileName() + ".tmp");
        RandomAccessFile tmpAccessFile = new RandomAccessFile(tmpFile, "rw");
        tmpAccessFile.setLength(contentLength);
        this.mTmpAccessFile = tmpFile;
    }

    public int getTaskState() {
        return mTaskState;
    }

    private void setTaskId(int mTaskId) {
        this.mTaskId = mTaskId;
    }

    private void setTaskState(int taskState) {
        this.mTaskState = taskState;
    }

    public Long getTaskTotalSize() {
        return mTaskTotalSize;
    }

    private void setTaskTotalSize(Long taskTotalSize) {
        this.mTaskTotalSize = taskTotalSize;
    }

    private void changeDownloadCacheById(int threadId, long l) {
        synchronized (mLock) {
            mDownloadFileCache.put(getThreadCacheStr(threadId), l);
        }
    }

    public long getDownloadCacheById(int threadId) {
        synchronized (mLock) {
            final Long value = mDownloadFileCache.get(getThreadCacheStr(threadId));
            return value == null ? DataUtils.getTheoryStartIndex(getTaskTotalSize(), threadId) : value;
        }
    }

    public void parseSegment(long contentLength, SegmentTaskEvaluator creator) {
        try {
            LogUtils.i("已获取文件长度" + contentLength);
            createTaskTmpFile(contentLength);

            for (int threadIndex = 0; threadIndex < BreakPointConst.DEFAULT_THREAD_COUNT; threadIndex++) {
                calculateSegmentPoint(threadIndex, creator);
            }

            LogUtils.d("开始执行分段下载,等待分段下载结束");
            getCountDownLatch().await();
            LogUtils.d("所有分段下载任务结束");

            requestDownloadSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            onTaskDownloadError(e.getMessage());
        }
    }

    private void calculateSegmentPoint(int threadId, SegmentTaskEvaluator creator) {
        // 线程开始下载的位置
        final long startIndex = getDownloadCacheById(threadId);

        final long intentStartIndex = DataUtils.getTheoryStartIndex(getTaskTotalSize(), threadId);
        LogUtils.d(threadId + "线程在本次文件下载的起点下标为" + (intentStartIndex + startIndex) + ",之前已下载了" + getDownloadCacheById(threadId));

        // 线程结束下载的位置
        final long endIndex = DataUtils.getTheoryEndIndex(getTaskTotalSize(), threadId);

        creator.startSegmentDownload(threadId, startIndex, endIndex);
    }

    @NotNull
    private String getThreadCacheStr(int threadId) {
        return "thread_" + threadId;
    }

    public long getSegmentFileSize(int threadId) {
        return DataUtils.getTheoryEndIndex(getTaskTotalSize(), threadId) - DataUtils.getTheoryStartIndex(getTaskTotalSize(), threadId) + 1;
    }

    boolean incompleteState() {
        return getTaskState() != BreakPointConst.DOWNLOAD_SUCCESS;
    }

    /**
     * 对外暴露的任务构建类
     * <p>
     * 只通过该类进行任务构建,新建出来的任务是没有id的,需要在添加的时候去生成
     *
     * @author wsb
     */
    public static class Builder {
        private String mTaskUrl;
        private String mTaskFileDir;
        private String mTaskFileName;

        /**
         * 将本地记录转换为任务
         *
         * @param recordEntity 本地记录的条目
         * @return 可执行的任务
         */
        public static Task transformRecord(Context context, TaskRecordEntity recordEntity) {
            final Task task = new Builder()
                    .setTaskUrl(recordEntity.getUrl())
                    .setTaskFileDir(recordEntity.getFileDir())
                    .setTaskFileName(recordEntity.getFileName()).build();
            task.setTaskState(recordEntity.getState());

            task.changeTaskCurrentSize(recordEntity.getCurrentSize());
            task.setTaskTotalSize(recordEntity.getTotalSize());
            task.setTaskId(recordEntity.getId());
            task.supplementField(context);
            return task;
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

    private void changeTaskCurrentSize(String currentSize) {
        try {
            final JSONObject jsonObject = new JSONObject(currentSize);
            final int length = jsonObject.length();
            mDownloadFileCache = new HashMap<>(length);

            for (int index = 0; index < length; index++) {
                changeDownloadCacheById(index, jsonObject.optLong(getThreadCacheStr(index)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分段任务估值器
     *
     * @author wsb
     */
    public interface SegmentTaskEvaluator {
        /**
         * 开启分段下载
         * <p>
         * 该方法将被多次调用
         *
         * @param threadId 线程下标
         * @param start    下载起点
         * @param end      下载终点
         */
        void startSegmentDownload(int threadId, long start, long end);
    }
}
