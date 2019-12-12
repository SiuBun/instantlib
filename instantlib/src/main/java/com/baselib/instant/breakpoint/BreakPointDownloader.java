package com.baselib.instant.breakpoint;

import android.content.Context;

import com.baselib.instant.breakpoint.bussiness.DownloadExecutor;
import com.baselib.instant.breakpoint.database.DataBaseRepository;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataCheck;
import com.baselib.instant.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    /**
     * 执行指定下载任务
     * <p>
     * 下载任务将经历以下阶段
     * <p>
     * 1.任务下载链接预检查阶段,确定链接是否可用,是否出现重定向 {@link Task#preload(Task.PreloadListener)}
     * <p>
     * 2.请求文件流得知完整文件大小,创建占位文件并明确分段下载起始点 {@link #getFileStream(Task, String)}
     * <p>
     * 3.解析文件流开始分段下载任务{@link #startSegmentDownload(Task, String, int, long, long)}
     *
     * @param context 上下文
     * @param task    任务对象
     */
    public void executeTask(Context context, Task task) {
        final Task.PreloadListener preloadListener = new Task.PreloadListener() {
            @Override
            public void preloadFail(String message) {
                task.onTaskPreloadFail("预加载失败");
            }

            @Override
            public void preloadSuccess(String downloadUrl) {
                task.postNewTaskSuccess();
                LogUtils.i("最终执行下载的链接为" + downloadUrl);
                getFileStream(task, downloadUrl);
            }
        };
        asyncExecute(task.preload(preloadListener));
    }

    private void getFileStream(Task task, String downloadUrl) {
        try {
            final FileStreamListener streamListener = new FileStreamListener() {

                @Override
                public void getFileStreamFail(String msg) {
                    task.onTaskDownloadError(msg);
                }

                @Override
                public void getFileStreamSuccess(long contentLength, InputStream byteStream) {
                    LogUtils.i("已获取文件长度" + contentLength);

                    parseSegmentByContentLength(task, downloadUrl, contentLength);
                }
            };


            mStreamProcessor.getCompleteFileStream(downloadUrl, streamListener);
        } catch (Exception e) {
            e.printStackTrace();
            task.onTaskDownloadError(e.getMessage());
        }
    }

    private void parseSegmentByContentLength(Task task, String downloadUrl, long contentLength) {
        try {
            final File tmpFile = new File(task.getTaskFileDir(), task.getTaskFileName() + ".tmp");
            RandomAccessFile tmpAccessFile = new RandomAccessFile(tmpFile, "rw");
            tmpAccessFile.setLength(contentLength);
            task.setTaskTmpFile(tmpFile);


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
                if (threadId == threadCount - 1) {
                    endIndex = contentLength;
                } else {
                    endIndex = (threadId + 1) * segmentSize;
                }
                endIndex = endIndex - 1;
                startSegmentDownload(task, downloadUrl, threadId, startIndex, endIndex);
            }
            LogUtils.d("开始执行分段下载,等待分段下载结束");
            task.getCountDownLatch().await();
            LogUtils.d("所有分段下载任务结束");
            task.requestDownloadFinish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分段下载执行
     *
     * @param task             任务对象
     * @param downloadUrl      下载链接
     * @param intentStartIndex 目标起点
     * @param intentEndIndex   目标终点
     */
    private void startSegmentDownload(Task task, String downloadUrl, int threadId, long intentStartIndex, long intentEndIndex) {
        LogUtils.d("开启线程分段下载:当前下载段为" + threadId + ",意图起终点分别是" + intentStartIndex + "," + intentEndIndex);
//        D: 开启线程分段下载:当前下载段为0,意图起终点分别是0,15960686
//        D: 开启线程分段下载:当前下载段为1,意图起终点分别是15960687,31921373
//        D: 开启线程分段下载:当前下载段为2,意图起终点分别是31921374,47882060
//        D: 开启线程分段下载:当前下载段为3,意图起终点分别是47882061,63842747
//        D: 开启线程分段下载:当前下载段为4,意图起终点分别是63842748,79803435

        final Runnable runnable = () -> {
            long cacheStartIndex = 0;
            final File cacheFile = new File(task.getTaskFileDir(), "thread" + threadId + "_" + task.getTaskFileName() + ".cache");
            task.setCacheFile(threadId, cacheFile);
            try {
                final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rw");

                try {
                    if (cacheFile.exists()) {
                        //重新设置下载起点
                        cacheStartIndex = Long.parseLong(cacheAccessFile.readLine());
                    }
                } catch (Exception e) {
                    LogUtils.w(e.getMessage());
                    cacheStartIndex = intentStartIndex;
                }


                long finalStartIndex = cacheStartIndex;
                final RangeDownloadListener rangeDownloadListener = new RangeDownloadListener() {
                    @Override
                    public void requestDownloadFail(String msg) {
                        task.onTaskDownloadError(msg);
                    }

                    @Override
                    public void requestDownloadFinish(long currentDownloadLength, long currentRangeFileLength) {
                        LogUtils.d(String.format(Locale.SIMPLIFIED_CHINESE, "%1d分段任务下载完成,线程的任务起点为%2d-本次共下载%3d byte,写至文件%4d处,分段文件总byte大小%5d",
                                threadId,
                                finalStartIndex,
                                currentDownloadLength,
                                currentRangeFileLength,
                                intentEndIndex - intentStartIndex + 1
                        ));
//                        之前已有下载内容
//                        D/BASE_LIB: 2分段任务下载完成,线程的任务起点为42345099-本次共下载5536962 byte,写至文件47882061处,分段文件总byte大小15960687
//                        D/BASE_LIB: 1分段任务下载完成,线程的任务起点为20105244-本次共下载11816130 byte,写至文件31921374处,分段文件总byte大小15960687
//                        D/BASE_LIB: 0分段任务下载完成,线程的任务起点为5209524-本次共下载10751163 byte,写至文件15960687处,分段文件总byte大小15960687
//                        D/BASE_LIB: 4分段任务下载完成,线程的任务起点为63842748-本次共下载15960688 byte,写至文件79803436处,分段文件总byte大小15960688
//                        D/BASE_LIB: 3分段任务下载完成,线程的任务起点为47882061-本次共下载15960687 byte,写至文件63842748处,分段文件总byte大小15960687

                        LogUtils.i("下载完成" + cacheFile.getName() + "将被删除:" + cacheFile.delete());
                        task.getCountDownLatch().countDown();
                    }

                    @Override
                    public void updateProgress(long currentRangeFileLength) throws IOException {

                        //将当前现在到的位置保存到文件中
                        cacheAccessFile.seek(0);
                        cacheAccessFile.write((String.valueOf(currentRangeFileLength)).getBytes(Charset.defaultCharset()));

                        final long progress = currentRangeFileLength - intentStartIndex;

                        task.onTaskProgressUpdate(threadId, progress);

                    }
                };
                mStreamProcessor.downloadRangeFile(downloadUrl, task.getTmpAccessFile(), cacheStartIndex, intentEndIndex, rangeDownloadListener);

            } catch (Exception e) {
                e.printStackTrace();
                task.onTaskDownloadError(e.getMessage());
            }
        };
        asyncExecute(runnable);

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
