package com.baselib.instant.breakpoint;

import java.io.File;

/**
 * 任务执行监听
 *
 * @author wsb
 */
public interface TaskPostListener {

    /**
     * 新任务添加成功
     * <p>
     * 该任务被添加到任务列表内,客户端可以在该方法内执行添加新任务完成的操作
     *
     * @param taskId 任务在列表内的id
     */
    void postNewTaskSuccess(int taskId);

    /**
     * 任务下载过程中出现异常
     * <p>
     * 客户端可以在该方法内进行下载任务进度出错时候的操作
     *
     * @param message 附带描述
     */
    void onTaskDownloadError(String message);

    /**
     * 任务进度更新
     * <p>
     * 客户端可以在该方法内进行下载任务进度更新的操作
     *
     * @param taskTotalSize 下载任务总体大小
     * @param length        任务总体的已下载长度
     */
    void onTaskProgressUpdate(long taskTotalSize, long length);

    /**
     * 任务下载完成
     * <p>
     * 所有分段任务下载完成时候回调,客户端可以在方法内执行下载完成操作
     *
     * @param file 所下载文件
     */
    void onTaskDownloadFinish(File file);

    /**
     * 任务取消
     */
    void onTaskCancel();

    /**
     * 任务下载开始
     *
     * @param downloadUrl   最终下载文件的链接
     * @param contentLength
     */
    void onTaskDownloadStart(String downloadUrl, long contentLength);

    /**
     * 任务下载暂停
     */
    void onTaskPause();
}