package com.baselib.instant.breakpoint.operate;

import java.io.IOException;

/**
 * 分段下载监听
 *
 * @author wsb
 */
public interface RangeDownloadListener {
    /**
     * 请求下载资源失败
     *
     * @param msg 附带消息
     */
    void rangeDownloadFail(String msg);

    /**
     * 请求下载资源完成
     *
     * @param currentDownloadLength  本次分段任务下载长度,如果之前已经下载过并被中断,会使本次下载长度小于等于文件总长度
     * @param currentRangeFileLength 分段任务在完整文件的写入终点
     */
    void rangeDownloadFinish(long currentDownloadLength, long currentRangeFileLength);

    /**
     * 下载进度更新
     *
     * @param currentDownloadLength  分段文件本次下载长度
     * @param rangeFileDownloadIndex 当前分段文件的写入的位置
     * @throws IOException 下载过程中可能出现未捕获的异常需要进行处理
     */
    void updateRangeProgress(long currentDownloadLength, long rangeFileDownloadIndex) throws IOException;

    /**
     * 当前是否能继续文件流写入
     *
     * @return true代表可以保持写入行为
     * */
    boolean canWrite();

    /**
     * 分段任务下载被结束
     * */
    void rangeDownloadOver();
}
