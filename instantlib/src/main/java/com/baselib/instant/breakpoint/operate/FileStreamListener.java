package com.baselib.instant.breakpoint.operate;

import android.support.annotation.WorkerThread;

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
    @WorkerThread
    void getFileStreamFail(String msg);

    /**
     * 流获取成功回调
     *
     * @param contentLength 文件流长度
     * @param byteStream    文件流对象
     */
    @WorkerThread
    void getFileStreamSuccess(long contentLength, InputStream byteStream);
}
