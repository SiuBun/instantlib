package com.baselib.instant.breakpoint;

import android.content.Context;

import com.baselib.instant.breakpoint.bussiness.DownloadExecutor;
import com.baselib.instant.breakpoint.database.DataBaseRepository;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataCheck;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 断点下载执行类
 *
 * @author wsb
 */
class BreakPointDownloader {

    private DataBaseRepository mDatabaseRepository;

    private StreamProcessor mStreamProcessor = new OkHttpSteamProcessor();

    private final DownloadExecutor mExecutor = new DownloadExecutor();

    public void executeTask(Context context, Task task) {
        final Task.PreloadListener preloadListener = new Task.PreloadListener() {
            @Override
            public void preloadFail(String message) {
                task.onTaskPreloadFail("预加载失败");
            }

            @Override
            public void preloadSuccess(String downloadUrl) {
                LogUtils.i("最终执行下载的链接为" + downloadUrl);

                getFileStream(context, task, downloadUrl);
            }
        };
        asyncExecute(task.preload(preloadListener));
    }

    private void getFileStream(Context context, Task task, String downloadUrl) {
        try {
            final FileStreamListener streamListener = new FileStreamListener() {

                @Override
                public void getFileStreamFail(String msg) {
                    task.onTaskDownloadError(msg);
                }

                @Override
                public void getFileStreamSuccess(long contentLength, InputStream byteStream) {
                    LogUtils.i("已获取文件长度" + contentLength);

                    parseFileInputStream(task, contentLength, byteStream);
                }
            };


            mStreamProcessor.getCompleteFileStream(downloadUrl, streamListener);
        } catch (Exception e) {
            e.printStackTrace();
            task.onTaskDownloadError(e.getMessage());
        }
    }

    private void parseFileInputStream(Task task, long contentLength, InputStream byteStream) {
        final File tmpFile = new File(task.getTaskFileDir(), task.getTaskFileName() + ".tmp");
        if (!tmpFile.getParentFile().exists()) {
            tmpFile.getParentFile().mkdirs();
        }
        try {
            RandomAccessFile tmpAccessFile = new RandomAccessFile(tmpFile, "rw");
            tmpAccessFile.setLength(contentLength);
            // 计算每个线程理论上下载的数量.
            final int threadCount = BreakPointConst.DEFAULT_THREAD_COUNT;
//            分段文件理论大小
            final long segmentSize = contentLength / threadCount;
            for (int threadId = 0; threadId < threadCount; threadId++) {
                // 线程开始下载的位置
                long startIndex = threadId * segmentSize;
                // 线程结束下载的位置
                long endIndex;
                // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
                if(threadId == threadCount -1){
                    endIndex = contentLength;
                }else {
                    endIndex = (threadId + 1) * segmentSize;
                }
                endIndex = endIndex-1;
                startSegmentDownload(threadId,startIndex,endIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            task.onTaskDownloadError(e.getMessage());
        }
    }

    private void startSegmentDownload(int threadId, long startIndex, long endIndex) {
        LogUtils.d("开启线程分段下载:当前下载段为"+threadId+",起始分别是"+startIndex+","+endIndex);
        asyncExecute(()->{

        });

    }

    private void asyncExecute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    public void loadTaskRecord(Context context, LoadLocalTaskListener listener) {
        final Context applicationContext = context.getApplicationContext();
        mDatabaseRepository = new DataBaseRepository(applicationContext);
        asyncExecute(() -> {
            final List<TaskRecordEntity> taskRecords = mDatabaseRepository.loadAllTaskRecord();
            DataCheck.checkEmpty(taskRecords);

            DataCheck.checkNoEmptyWithCallback(taskRecords, data -> {
                Map<Integer, Task> taskMap = new HashMap<>(BreakPointConst.DEFAULT_CAPACITY);
                for (TaskRecordEntity recordEntity : data) {
                    final Task task = Task.Builder.transformRecord(recordEntity);
                    taskMap.put(task.getTaskId(), task);
                }
                listener.localTaskExist(taskMap);
            });
        });
    }

    /**
     * 加载本地记录监听
     *
     * @author wsb
     */
    interface LoadLocalTaskListener {
        /**
         * 本地存在往期任务
         *
         * @param taskMap 往期任务内容
         */
        void localTaskExist(Map<Integer, Task> taskMap);
    }


}
