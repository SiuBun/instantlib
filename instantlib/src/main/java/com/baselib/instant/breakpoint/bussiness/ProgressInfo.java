package com.baselib.instant.breakpoint.bussiness;

import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.util.DataCheck;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下载任务进展信息
 *
 * @author wsb
 */
public class ProgressInfo {
    /**
     * 任务状态
     */
    private int mTaskState;

    private final byte[] mLock = new byte[0];

    /**
     * 记录各任务已下载长度
     */
    private Map<String, Long> mDownloadFileCache;

    private CountDownLatch mCountDownLatch;

    /**
     * 下载的文件,下载完成以前以占位文件形式保存,下载完成后修改文件类型
     */
    private File mTmpFile;

    public ProgressInfo() {
        mTaskState = BreakPointConst.DOWNLOAD_WAITING;
        mDownloadFileCache = initDownloadFileCache();
    }

    /**
     * 新创建的任务在默认情况下，使用指定个数的分任务下载
     */
    private Map<String, Long> initDownloadFileCache() {
//        Map<String, Long> map;
//        try {
//            map = rebuildFileCacheMap(new JSONObject(""));
//        } catch (JSONException e) {
//            map = new HashMap<>(BreakPointConst.DEFAULT_THREAD_COUNT);
//            e.printStackTrace();
//        }

        Map<String, Long> map = new HashMap<>(BreakPointConst.DEFAULT_THREAD_COUNT);
        for (int threadIndex = 0; threadIndex < BreakPointConst.DEFAULT_THREAD_COUNT; threadIndex++) {
            map.put(DataUtils.getThreadCacheStr(threadIndex), 0L);
        }
        return map;
    }

    /**
     * 获取分段下载的线程数
     */
    public int getDownloadThreadCount() {
        return mDownloadFileCache.size() == 0 ? BreakPointConst.DEFAULT_THREAD_COUNT : mDownloadFileCache.size();
    }

    public long getTaskDownloadedLength() {
        synchronized (mLock) {
            long totalLength = 0;
            for (Map.Entry<String, Long> entry : mDownloadFileCache.entrySet()) {
                totalLength += entry.getValue();
            }
            return totalLength;
        }
    }

    public String getTaskCurrentSizeJson() {
        synchronized (mLock) {
            final JSONObject jsonObject = new JSONObject();

            try {
                for (Map.Entry<String, Long> entry : mDownloadFileCache.entrySet()) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }
    }

    public void changeTaskCurrentSizeFromCache(String currentSize) {
        Map<String, Long> map;
        try {
            map = rebuildFileCacheMap(new JSONObject(currentSize));
        } catch (JSONException e) {
            e.printStackTrace();
            map = new HashMap<>(BreakPointConst.DEFAULT_THREAD_COUNT);
        }
        mDownloadFileCache = map;
    }

    /**
     * 旧任务在读取本地缓存后确定分任务的个数
     *
     * @param jsonObject 本地缓存的json对象
     * @return json恢复结果
     */
    private Map<String, Long> rebuildFileCacheMap(JSONObject jsonObject) {
        Map<String, Long> map = new HashMap<>(jsonObject.length());
        for (int index = 0; index < jsonObject.length(); index++) {
            String threadCacheKey = DataUtils.getThreadCacheStr(index);
            map.put(threadCacheKey, jsonObject.optLong(threadCacheKey));
        }
        return map;
    }

    public void changeDownloadCacheById(int threadId, long length) {
        synchronized (mLock) {
            mDownloadFileCache.put(DataUtils.getThreadCacheStr(threadId), length);
        }
    }

    public long getDownloadCacheById(int threadId) {
        Long value = mDownloadFileCache.get(DataUtils.getThreadCacheStr(threadId));
        long cacheLength;
        if (null == value) {
            cacheLength = 0L;
        } else {
            cacheLength = value;
        }
        return cacheLength;
    }

    public long getDownloadStartIndexById(int threadId, long totalSize) {
        synchronized (mLock) {
            AtomicLong theoryStartIndex = new AtomicLong(DataUtils.getTheoryStartIndex(totalSize, getDownloadThreadCount(), threadId));
            DataCheck.checkNoNullWithCallback(mDownloadFileCache.get(DataUtils.getThreadCacheStr(threadId)), theoryStartIndex::addAndGet);
            return theoryStartIndex.get();
        }
    }

    public int getTaskState() {
        synchronized (mLock){
            return mTaskState;
        }
    }

    public void setTaskState(int taskState) {
        synchronized (mLock){
            this.mTaskState = taskState;
        }
    }

    public CountDownLatch getCountDownLatch() {
        if (mCountDownLatch == null){
            mCountDownLatch = new CountDownLatch(mDownloadFileCache.size());
        }
        return mCountDownLatch;
    }

    public synchronized File getTmpFile() {
        return mTmpFile;
    }

    public synchronized void setTmpFile(File file) {
        mTmpFile = file;
    }

    public synchronized boolean renameFile(String taskFileDir, String taskFileName) {
        boolean renameFile = false;
        final File dest = new File(taskFileDir, taskFileName);
        if(mTmpFile.renameTo(dest)){
            mTmpFile = dest;
            renameFile = true;
        }
        return renameFile;
    }

    public synchronized boolean deleteFile() {
        return mTmpFile.exists() && mTmpFile.delete();
    }

    public void resetCountDown() {
        mCountDownLatch = null;
    }
}
