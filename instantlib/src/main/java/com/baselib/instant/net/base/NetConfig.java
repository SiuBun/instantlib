package com.baselib.instant.net.base;

/**
 * 对网络请求管理对象的相关设置
 * <p>
 * 含超时，缓存策略等
 *
 * @author wsb
 */
public class NetConfig {
    private static final int DEFAULT_TIMEOUT = 15;
    private static final int DEFAULT_RETRY_COUNT = 1;
    private int mRetryCount;

    public NetConfig() {
        mTimeout = DEFAULT_TIMEOUT;
        mRetryCount = DEFAULT_RETRY_COUNT;
    }

    private int mTimeout;

    public int getTimeout() {
        return mTimeout;
    }

    public void setTimeout(int timeOut) {
        this.mTimeout = timeOut;
    }

    public void setRetryCount(int retryCount) {
        this.mRetryCount = retryCount;
    }

    public int getRetryCount() {
        return mRetryCount;
    }
}
