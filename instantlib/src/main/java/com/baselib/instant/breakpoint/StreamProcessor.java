package com.baselib.instant.breakpoint;

/**
 * 流处理对象标准操作
 *
 * @author wsb
 */
interface StreamProcessor {

    /**
     * 获取下载文件完整的流
     * <p>
     * 确保该方法被调用在子线程中,主线程获取将导致阻塞
     *
     * @param url            下载链接
     * @param streamListener 文件流获取监听对象
     * @throws Exception 流处理过程中可能出现异常需要被捕获处理
     */
    void getCompleteFileStream(String url, FileStreamListener streamListener) throws Exception;


}
