package com.baselib.instant.breakpoint.bussiness;


import com.baselib.instant.util.LogUtils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 处理下载任务的线程池
 *
 * 线程池核心线程数取决于当前核数
 *
 * @author wsb
 * */
public class DownloadExecutor extends ThreadPoolExecutor {

    public static final String TAG = "DownloadExecutor";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
//    private static final int CORE_POOL_SIZE = Math.max(3, CPU_COUNT / 2);
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME = 0L;

    public DownloadExecutor() {
        super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
    }

}
