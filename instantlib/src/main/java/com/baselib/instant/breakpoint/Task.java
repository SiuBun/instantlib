package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.io.RandomAccessFile;
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
    private long[] mDownloadFileLength;

    private final CountDownLatch mCountDownLatch;
    private int mTaskState;
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
        mCacheFiles = new File[BreakPointConst.DEFAULT_THREAD_COUNT];
        mDownloadFileLength = new long[BreakPointConst.DEFAULT_THREAD_COUNT];
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
    public void supplementField(Context context) {
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

//        任务的唯一标识,根据下载url和保存位置来生成
        this.mTaskId = DataUtils.generateId(getTaskUrl(), getTaskPath());
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

    /**
     * 提交任务成功通知所有监听对象
     */
    public void postNewTaskSuccess() {
        for (TaskListener listener : mTaskListenerSet) {
            listener.postNewTaskSuccess(getTaskId());
        }
    }

    /**
     * 预加载成功通知所有监听对象
     */
    public void onTaskPreloadSuccess() {
        setTaskState(BreakPointConst.DOWNLOAD_PREPARED);
    }

    /**
     * 预加载失败通知所有监听对象
     *
     * @param msg 附带消息
     */
    public void onTaskPreloadFail(String msg) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskDownloadError(msg);
        }
    }

    /**
     * 任务下载过程出错,通知所有监听对象
     *
     * @param message 附带消息
     */
    public void onTaskDownloadError(String message) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskDownloadError(message);
        }
    }

    /**
     * 下载进度更新通知所有监听对象
     *
     * @param length 已下载的文件长度
     */
    public void onTaskProgressUpdate(long length) {
        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskProgressUpdate(getTaskTotalSize(), length);
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
        setDownloadedLength(threadId, threadDownloadLength);
        long totalLength = 0;

        for (long len : mDownloadFileLength) {
            totalLength += len;
        }
        onTaskProgressUpdate(totalLength);
    }


    public void requestDownloadFinish() {
        mTmpAccessFile.renameTo(new File(getTaskFileDir(), getTaskFileName()));

        for (TaskListener listener : mTaskListenerSet) {
            listener.onTaskDownloadFinish();
        }
    }

    public TaskRecordEntity parseToRecord() {
        final TaskRecordEntity recordEntity = new TaskRecordEntity();
        recordEntity.setCurrentSize(0L);
        recordEntity.setFileDir(getTaskFileDir());
        recordEntity.setFileName(getTaskFileName());
        recordEntity.setId(getTaskId());
        recordEntity.setState(getTaskState());
        recordEntity.setTotalSize(getTaskTotalSize());
        recordEntity.setUpdateTime(System.currentTimeMillis());
        recordEntity.setUrl(getTaskUrl());
        return recordEntity;
    }


    public void createTaskTmpFile(long contentLength) throws Exception {
        setTaskTotalSize(contentLength);
        //            创建占位文件并设置长度
        final File tmpFile = new File(getTaskFileDir(), getTaskFileName() + ".tmp");
        RandomAccessFile tmpAccessFile = new RandomAccessFile(tmpFile, "rw");
        tmpAccessFile.setLength(contentLength);
        this.mTmpAccessFile = tmpFile;
    }

    public File[] getCacheFiles() {
        return mCacheFiles;
    }

    public void setDownloadedLength(int threadId, long cacheStartIndex) {
        mDownloadFileLength[threadId] = cacheStartIndex;
    }

    public int getTaskState() {
        return mTaskState;
    }

    public void setTaskState(int taskState) {
        this.mTaskState = taskState;
    }

    public Long getTaskTotalSize() {
        return mTaskTotalSize;
    }

    public void setTaskTotalSize(Long taskTotalSize) {
        this.mTaskTotalSize = taskTotalSize;
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
            requestDownloadFinish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateSegmentPoint(int threadId, SegmentTaskEvaluator creator) {
        final File cacheFile = new File(getTaskFileDir(), "thread" + threadId + "_" + getTaskFileName() + ".cache");
        mCacheFiles[threadId] = cacheFile;

        // 线程开始下载的位置
        final long startIndex = getStartIndex(cacheFile, getTaskTotalSize(), threadId);
        setDownloadedLength(threadId, startIndex);
        // 线程结束下载的位置
        final long endIndex = getEndIndex(getTaskTotalSize(), threadId);

        creator.startSegmentDownload(threadId, startIndex, endIndex);
    }

    /**
     * 获取分段任务下载终点
     *
     * @param contentLength 文件长度
     * @param threadId      线程下标
     */
    private long getEndIndex(long contentLength, int threadId) {
        final long segmentSize = contentLength / BreakPointConst.DEFAULT_THREAD_COUNT;
        long endIndex;
        // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
        if (threadId == BreakPointConst.DEFAULT_THREAD_COUNT - 1) {
            endIndex = contentLength;
        } else {
            endIndex = (threadId + 1) * segmentSize;
        }
        endIndex = endIndex - 1;
        return endIndex;
    }

    /**
     * 获取分段任务下载起点
     * <p>
     * 如果本地存在缓存文件,那么从缓存文件获取该任务已下载的长度.文件不存在或者读取失败就计算该分段任务的理论起点
     *
     * @param cacheFile     缓存任务
     * @param contentLength 文件大小
     * @param threadId      线程下标
     * @return 返回真正写入的起点
     */
    private long getStartIndex(File cacheFile, long contentLength, int threadId) {
        final long segmentSize = contentLength / BreakPointConst.DEFAULT_THREAD_COUNT;
        long cacheStartIndex;
        final long intentStart = threadId * segmentSize;
        try {
            if (cacheFile.exists()) {
                final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rw");
                //重新设置下载起点
                cacheStartIndex = Long.parseLong(cacheAccessFile.readLine());
            } else {
                cacheStartIndex = intentStart;
            }
        } catch (Exception e) {
            e.printStackTrace();
            cacheStartIndex = intentStart;
        }
        return cacheStartIndex;
    }

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
        public static Task transformRecord(TaskRecordEntity recordEntity) {
            final Task task = new Builder()
                    .setTaskUrl(recordEntity.getUrl())
                    .setTaskFileDir(recordEntity.getFileDir())
                    .setTaskFileName(recordEntity.getFileName()).build();
            task.setTaskState(recordEntity.getState());
            task.setTaskTotalSize(recordEntity.getTotalSize());
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

    /**
     * 分段任务估值器
     *
     * @author wsb
     * */
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
