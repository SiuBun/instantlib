package com.baselib.instant.breakpoint.operate;

/**
 * 分段任务估值器
 *
 * @author wsb
 */
public interface SegmentTaskEvaluator {
    /**
     * 开启分段下载
     * <p>
     * 该方法将被各个分段任务调用
     *
     * @param threadId 线程下标
     * @param start    下载起点
     * @param end      下载终点
     */
    void startSegmentDownload(int threadId, long start, long end);

    /**
     * 获取分段下载监听对象
     *
     * @param threadId 线程下标
     * @param start    下载起点
     * @param end      下载终点
     * @return 分段下载监听
     */
    RangeDownloadListener getRangeDownloadListener(int threadId, long start, long end);

    /**
     * 请求下载完成回调
     * */
    void requestDownloadSuccess();
}