package com.baselib.instant.breakpoint.utils;

/**
 * 断点工具常量
 *
 * @author wsb
 * */
public interface BreakPointConst {

    int DEFAULT_CAPACITY = 10;

    /**
     * 下载目录名
     */
    String DIR_NAME = "bp_download";

    /**
     * 本地数据库名称
     */
    String DB_NAME = "task_database";

    /**
     * 任务前置工作已经完成
     * */
    int DOWNLOAD_PREPARED = 0;
    /**
     * 任务下载开始
     * */
    int DOWNLOAD_START = 0x1;

    /**
     * 任务下载进度更新
     * */
    int DOWNLOAD_PROGRESS_UPDATE = 0x1 << 1;

    /**
     * 任务下载暂停
     * */
    int DOWNLOAD_PAUSE = 0x1 << 2;

    /**
     * 任务下载错误
     * */
    int DOWNLOAD_ERROR = 0x1 << 3;

    /**
     * 任务下载取消
     * */
    int DOWNLOAD_CANCEL = 0x1 << 4;

    /**
     * 任务下载成功
     * */
    int DOWNLOAD_SUCCESS = 0x1 << 5;

    /**
     * 任务下载完成
     * */
    int DOWNLOAD_FINISH = 0x1 << 6;

    int REQ_SUCCESS = 200;

    int REQ_REFLECT = 302;

    int REQ_RANGE_SUCCESS = 206;

    int DEFAULT_THREAD_COUNT = 4;
}
