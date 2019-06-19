package com.baselib.instant.net.base;

import java.io.IOException;

/**
 * 网络请求状态回调
 *
 * @author wsb
 */
public interface IHttpStateCallback {
    /**
     * 请求开始之前
     *
     * @param url 最后请求的链接
     */
    void reqBefore(String url);

    /**
     * 请求开始
     * */
    void reqStart();

    /**
     * 请求成功
     * @param s 从服务器返回的内容
     * */
    void reqSuccess(String s);

    /**
     * 请求失败
     * @param failInfo 失败信息
     * */
    void reqFail(String failInfo);

    /**
     * 请求错误
     * @param e 所出现的异常
     * */
    void reqError(IOException e);

    /**
     * 请求完成
     * */
    void reqFinish();
}
