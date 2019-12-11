package com.baselib.instant.breakpoint;

import java.io.InputStream;

/**
 * 文件流获取监听
 *
 * @author wsb
 */
public interface FileStreamListener {
    /**
     * 流获取失败
     *
     * @param msg 失败信息
     */
    void getFileStreamFail(String msg);

    /**
     * 流获取成功回调
     *
     * @param contentLength 文件流长度
     * @param byteStream    文件流对象
     */
    void getFileStreamSuccess(long contentLength, InputStream byteStream);
}
