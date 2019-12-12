package com.baselib.instant.breakpoint;

import java.io.IOException;

/**
 * 分段下载监听
 *
 * @author wsb
 * */
public interface RangeDownloadListener {
    /**
     * 请求下载资源失败
     *
     * @param msg 附带消息
     * */
    void requestDownloadFail(String msg);

    /**
     * 请求下载资源完成
     *
     * @param currentDownloadLength 本次分段任务下载长度,如果之前已经下载过并被中断,会使本次下载长度小于等于文件总长度
     * @param currentRangeFileLength 分段任务在完整文件的写入终点
     * */
    void requestDownloadFinish(long currentDownloadLength, long currentRangeFileLength);

    /**
     * 下载进度更新
     *
     * @param currentRangeFileLength
     * */
    void updateProgress(long currentRangeFileLength) throws IOException;
}
