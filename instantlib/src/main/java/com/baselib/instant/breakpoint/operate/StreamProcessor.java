package com.baselib.instant.breakpoint.operate;

import java.io.File;

import androidx.annotation.WorkerThread;

/**
 * 流处理对象标准操作
 *
 * @author wsb
 */
public interface StreamProcessor {

    /**
     * 获取下载文件完整的流
     * <p>
     * 确保该方法被调用在子线程中,主线程获取将导致阻塞
     *
     * @param url            下载链接
     * @param streamListener 文件流获取监听对象
     */
    @WorkerThread
    void getCompleteFileStream(String url, FileStreamListener streamListener);

    /**
     * 下载指定起终点的分段文件
     *
     * @param url              下载链接
     * @param tmpAccessFile    所写入的文件
     * @param startIndex       实际写入起点
     * @param endIndex         实际写入终点
     * @param downloadListener 分段下载监听
     * @throws Exception 流处理过程中可能出现异常需要被捕获处理
     */
    @WorkerThread
    void downloadRangeFile(String url, File tmpAccessFile, long startIndex, long endIndex,
                           RangeDownloadListener downloadListener) throws Exception;
}
