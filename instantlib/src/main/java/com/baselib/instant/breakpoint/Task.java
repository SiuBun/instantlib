package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.baselib.instant.breakpoint.bussiness.ProgressInfo;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.operate.PreloadListener;
import com.baselib.instant.breakpoint.operate.SegmentTaskEvaluator;
import com.baselib.instant.breakpoint.operate.TaskListenerOperate;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 下载的任务对象
 *
 * @author wsb
 */
public class Task implements TaskListenerOperate {

    private final ProgressInfo mProgressInfo;
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
     * 文件总大小
     */
    private Long mTaskFileTotalSize;

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
        mProgressInfo = new ProgressInfo();
    }


    public File getTmpFile() {
        return mProgressInfo.getTmpFile();
    }

    public CountDownLatch getCountDownLatch() {
        return mProgressInfo.getCountDownLatch();
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

    @Override
    public boolean addTaskListener(TaskPostListener listener) {
        synchronized (mLock) {
            return mTaskListenerSet.add(listener);
        }
    }

    @Override
    public boolean removeTaskListener(TaskPostListener listener) {
        synchronized (mLock) {
            return mTaskListenerSet.remove(listener);
        }
    }

    @Override
    public void cleanTaskListener() {
        synchronized (mLock) {
            mTaskListenerSet.clear();
        }
    }

    @Override
    public Collection<TaskPostListener> getTaskListener() {
        return this.mTaskListenerSet;
    }

    @Override
    public void addTaskListeners(Collection<TaskPostListener> listeners) {
        synchronized (mLock) {
            mTaskListenerSet.addAll(listeners);
        }
    }

    public void onAppDetach() {

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
            listener.onTaskProgressUpdate(getTaskFileTotalSize(), length);
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
     * @param threadId               线程id
     * @param rangeFileDownloadIndex 本次分段任务已下载长度
     */
    public void onRangeFileProgressUpdate(int threadId, long rangeFileDownloadIndex) {
        long currentRangeLength = rangeFileDownloadIndex - DataUtils.getTheoryStartIndex(getTaskFileTotalSize(), getDownloadThreadCount(), threadId);
        changeDownloadCacheById(threadId, currentRangeLength);
        onTaskProgressUpdate(mProgressInfo.getTaskDownloadedLength());
    }


    private void requestDownloadSuccess() {
        setTaskState(BreakPointConst.DOWNLOAD_SUCCESS);
        if (mProgressInfo.renameFile(getTaskFileDir(),getTaskFileName())) {
            for (TaskPostListener listener : mTaskListenerSet) {
                listener.onTaskDownloadFinish();
            }
        } else {
            LogUtils.e("文件下载完成时修改名称失败");
        }
    }

    public String getTaskCurrentSizeJson() {
        return mProgressInfo.getTaskCurrentSizeJson();
    }

    /**
     * 创建占位文件并设置长度
     *
     * @param contentLength 文件总长度
     */
    private void createTaskTmpFile(long contentLength) {
        setTaskFileTotalSize(contentLength);
        mProgressInfo.setTmpFile(new File(getTaskFileDir(), getTaskFileName() + ".tmp"));
    }

    public int getTaskState() {
        return mProgressInfo.getTaskState();
    }

    private void setTaskId(int mTaskId) {
        this.mTaskId = mTaskId;
    }

    private void setTaskState(int taskState) {
        mProgressInfo.setTaskState(taskState);
    }

    public Long getTaskFileTotalSize() {
        return mTaskFileTotalSize;
    }

    private void setTaskFileTotalSize(Long taskTotalSize) {
        this.mTaskFileTotalSize = taskTotalSize;
    }

    public void parseSegment(long contentLength, SegmentTaskEvaluator creator) {
        try {
            LogUtils.i("已获取文件长度" + contentLength);
            createTaskTmpFile(contentLength);

            for (int threadIndex = 0; threadIndex < getDownloadThreadCount(); threadIndex++) {
                calculateSegmentPoint(threadIndex, creator);
            }

            LogUtils.d("开始执行分段下载,等待分段下载结束");
            getCountDownLatch().await();
            LogUtils.d("该下载任务所有分段任务结束");

            requestDownloadSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            onTaskDownloadError(e.getMessage());
        }
    }

    private void calculateSegmentPoint(int threadId, SegmentTaskEvaluator creator) {
        // 线程开始下载的位置
        final long startIndex = getDownloadStartIndexById(threadId);

        final long intentStartIndex = DataUtils.getTheoryStartIndex(getTaskFileTotalSize(), getDownloadThreadCount(), threadId);

        // 线程结束下载的位置
        final long endIndex = DataUtils.getTheoryEndIndex(getTaskFileTotalSize(), getDownloadThreadCount(), threadId);

        LogUtils.d(threadId + "线程在本次文件下载的理论起点下标为" + intentStartIndex + ",该文件会最后写在" + endIndex + "下标处,本地记录之前已下载了" + getDownloadCacheById(threadId) + " byte");
        if (startIndex == endIndex + 1) {
//            LogUtils.i(threadId + "不需要再进行重复下载");
            creator.getRangeDownloadListener(threadId, startIndex, endIndex).rangeDownloadFinish(0L, endIndex - intentStartIndex);
        } else {
//            LogUtils.i(threadId + "在本次下载的实际起点下标为" + startIndex);
            creator.startSegmentDownload(threadId, startIndex, endIndex);
        }
    }


    /**
     * 根据分段任务id获取该段任务所需下载的文件大小
     *
     * @param threadId 分段任务下标
     * @return 该段任务所需下载的文件大小
     */
    public long getSegmentFileSize(int threadId) {
        return DataUtils.getTheoryEndIndex(getTaskFileTotalSize(), getDownloadThreadCount(), threadId) - DataUtils.getTheoryStartIndex(getTaskFileTotalSize(), getDownloadThreadCount(), threadId) + 1;
    }

    boolean incompleteState() {
        return getTaskState() != BreakPointConst.DOWNLOAD_SUCCESS;
    }


    private void changeTaskCurrentSizeFromCache(String currentSize) {
        mProgressInfo.changeTaskCurrentSizeFromCache(currentSize);
    }

    /**
     * 改变当前分段任务的下载的长度
     * <p>
     * 切记不是改变写入的下标
     *
     * @param threadId 分段任务下标
     * @param length   分段任务当前下载的长度
     */
    private void changeDownloadCacheById(int threadId, long length) {
        mProgressInfo.changeDownloadCacheById(threadId, length);
    }

    /**
     * 获取指定分段任务的已下载长度
     *
     * @param threadId 分段任务下标
     * @return 该分段任务已下载长度
     */
    private long getDownloadCacheById(int threadId) {
        return mProgressInfo.getDownloadCacheById(threadId);
    }

    /**
     * 获取文件下载实际起点
     *
     * @param threadId 分段任务下标
     * @return 实际下载的起点
     */
    private long getDownloadStartIndexById(int threadId) {
        return mProgressInfo.getDownloadStartIndexById(threadId, getTaskFileTotalSize());
    }

    /**
     * 获取分段下载的线程数
     */
    private int getDownloadThreadCount() {
        return mProgressInfo.getDownloadThreadCount();
    }

    /**
     * 对外暴露的任务构建类
     * <p>
     * 只通过该类进行任务构建,新建出来的任务是没有id的,需要在添加的时候去生成
     * <p>
     * 由该类负责内存中任务和本地的数据库的交互,如{@link #parseToRecord(Task)}和{@link #transformRecord(Context, TaskRecordEntity)}方法
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

            task.changeTaskCurrentSizeFromCache(recordEntity.getCurrentSize());
            task.setTaskFileTotalSize(recordEntity.getTotalSize());
            task.setTaskId(recordEntity.getId());
            task.supplementField(context);
            return task;
        }

        /**
         * 将任务转换为本地记录
         *
         * @param task 内存中的下载任务
         * @return 本地记录
         */
        public static TaskRecordEntity parseToRecord(Task task) {
            final TaskRecordEntity recordEntity = new TaskRecordEntity();
            recordEntity.setCurrentSize(task.getTaskCurrentSizeJson());
            recordEntity.setFileDir(task.getTaskFileDir());
            recordEntity.setFileName(task.getTaskFileName());
            recordEntity.setId(task.getTaskId());
            recordEntity.setState(task.getTaskState());
            recordEntity.setTotalSize(task.getTaskFileTotalSize());
            recordEntity.setUpdateTime(System.currentTimeMillis());
            recordEntity.setUrl(task.getTaskUrl());
            return recordEntity;
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


}
