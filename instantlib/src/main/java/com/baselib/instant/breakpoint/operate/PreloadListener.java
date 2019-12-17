package com.baselib.instant.breakpoint.operate;

/**
     * 预加载任务监听对象
     *
     * @author wsb
     */
    public interface PreloadListener {
        /**
         * 预加载过程出错
         *
         * @param message 错误信息
         */
        void preloadFail(String message);

        /**
         * 预加载成功
         *
         * @param realDownloadUrl 实际下载链接
         */
        void preloadSuccess(String realDownloadUrl);
    }