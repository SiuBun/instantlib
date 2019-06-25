package com.baselib.instant.net.base;

import java.io.File;

/**
 * 对网络请求管理对象的相关设置
 * <p>
 * 含超时，缓存策略等
 *
 * @author wsb
 */
public class NetConfig {
    public static final long DEFAULT_CACHE_SIZE = 1024 * 1024;
    public static final int DEFAULT_CACHE_VALID_TIME = 20;
    public static final int DEFAULT_CACHE_STALE_TIME = 10;
    private static final int DEFAULT_TIMEOUT = 15;
    private static final int DEFAULT_RETRY_COUNT = 1;
    private int mRetryCount;
    private File mCacheFile;
    private int mTimeout;

    public NetConfig() {
        mTimeout = DEFAULT_TIMEOUT;
        mRetryCount = DEFAULT_RETRY_COUNT;
    }

    public void setCacheFile(File mCacheFile) {
        this.mCacheFile = mCacheFile;
    }

    public File getCacheFile() {
        return mCacheFile;
    }

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
