package com.baselib.instant.breakpoint.bussiness;

import android.content.Context;
import android.os.Looper;

import com.baselib.instant.breakpoint.Task;
import com.baselib.instant.breakpoint.database.DataBaseRepository;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.operate.FileStreamListener;
import com.baselib.instant.breakpoint.operate.PreloadListener;
import com.baselib.instant.breakpoint.operate.RangeDownloadListener;
import com.baselib.instant.breakpoint.operate.StreamProcessor;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.manager.BusinessHandler;
import com.baselib.instant.util.DataCheck;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 断点下载执行类
 *
 * @author wsb
 */
public class BreakPointDownloader {

    private DataBaseRepository mDatabaseRepository;

    private StreamProcessor mStreamProcessor = new OkHttpSteamProcessor();

    private final DownloadExecutor mExecutor = new DownloadExecutor();

    private final BusinessHandler mHandler = new BusinessHandler(Looper.getMainLooper(), msg -> {
    });

    /**
     * 执行指定下载任务
     * <p>
     * 下载任务将经历以下阶段
     * <p>
     * 1.任务下载链接预检查阶段,确定链接是否可用,是否出现重定向 {@link Task#preload(PreloadListener)}
     * <p>
     * 2.请求文件流得知完整文件大小{@link #getFileStream(Task, String)},创建占位文件并明确分段下载起始点
     * <p>
     * 3.解析文件流开始分段下载任务{@link Task#parseSegment(long, Task.SegmentTaskEvaluator)}
     *
     * @param context 上下文
     * @param task    任务对象
     */
    public void executeTask(Context context, Task task) {
        final PreloadListener preloadListener = new PreloadListener() {
            @Override
            public void preloadFail(String message) {
                mainThreadExecute(() -> task.onTaskPreloadFail("预加载失败," + message));
            }

            @Override
            public void preloadSuccess(String realDownloadUrl) {
                mainThreadExecute(() -> task.onTaskPreloadSuccess(realDownloadUrl));
                getFileStream(task, realDownloadUrl);
            }
        };
        asyncExecute(task.preload(preloadListener));
    }

    private void getFileStream(Task task, String downloadUrl) {
        try {
            final FileStreamListener streamListener = new FileStreamListener() {

                @Override
                public void getFileStreamFail(String msg) {
                    mainThreadExecute(() -> task.onTaskDownloadError("文件流获取出错: " + msg));
                }

                @Override
                public void getFileStreamSuccess(long contentLength, InputStream byteStream) {
                    mainThreadExecute(() -> task.onTaskDownloadStart(downloadUrl));

//                    文件分段下载方案
                    task.parseSegment(contentLength, getSegmentTaskEvaluator(task, downloadUrl));
                }
            };

            mStreamProcessor.getCompleteFileStream(downloadUrl, streamListener);
        } catch (Exception e) {
            e.printStackTrace();
            mainThreadExecute(() -> task.onTaskDownloadError(e.getMessage()));
        }
    }

    @NotNull
    private Task.SegmentTaskEvaluator getSegmentTaskEvaluator(Task task, String downloadUrl) {
        return new Task.SegmentTaskEvaluator() {
            @Override
            public void startSegmentDownload(int threadId, long start, long end) {
                RangeDownloadListener rangeDownloadListener = getRangeDownloadListener(threadId, start, end);
                asyncExecute(
                        getSegmentRunnable(start, end, downloadUrl, task, rangeDownloadListener)
                );
            }

            @Override
            public RangeDownloadListener getRangeDownloadListener(int threadId, long start, long end) {
                return buildRangeDownloadListener(task, threadId, start, end);
            }
        };
    }

    @NotNull
    private Runnable getSegmentRunnable(long start, long end, String downloadUrl, Task task, RangeDownloadListener rangeDownloadListener) {
        return () -> {
            try {
                mStreamProcessor.downloadRangeFile(downloadUrl, task.getTmpFile(), start, end, rangeDownloadListener);
            } catch (Exception e) {
                e.printStackTrace();
                rangeDownloadListener.rangeDownloadFail(e.getMessage());
            }
        };
    }

    @NotNull
    private RangeDownloadListener buildRangeDownloadListener(Task task, int threadId, long realStartIndex, long realEndIndex) {
        return new RangeDownloadListener() {
            @Override
            public void rangeDownloadFail(String msg) {
                mainThreadExecute(() -> task.onTaskDownloadError(threadId + "分段任务下载过程出错," + msg));
            }

            @Override
            public void rangeDownloadFinish(long currentDownloadLength, long currentRangeFileLength) {
                LogUtils.d(String.format(Locale.SIMPLIFIED_CHINESE, "%1d分段任务下载完成,线程的任务从%2d处续写-本次共下载%3d byte,内容最后写入了文件%4d下标,该分段文件总byte大小%5d",
                        threadId,
                        realStartIndex,
                        currentDownloadLength,
                        realStartIndex+currentDownloadLength-1,
                        task.getSegmentFileSize(threadId)
                ));
                LogUtils.i(threadId + "线程代表的下载任务完成");
                mDatabaseRepository.updateTaskRecord(task.parseToRecord());
                task.getCountDownLatch().countDown();
            }

            @Override
            public void updateRangeProgress(long currentDownloadLength,long rangeFileDownloadIndex) {
                mainThreadExecute(() -> task.onRangeFileProgressUpdate(threadId, rangeFileDownloadIndex));
            }
        };
    }

    private void asyncExecute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    public void mainThreadExecute(Runnable runnable) {
        mHandler.post(runnable);
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
                    final Task task = Task.Builder.transformRecord(applicationContext, recordEntity);
                    taskMap.put(task.getTaskId(), task);
                }
                listener.localTaskExist(taskMap);
            });
        });
    }

    public void onNewTaskAdd(Task task) {
        asyncExecute(() -> mDatabaseRepository.addTaskRecord(task.parseToRecord()));
    }

    public void deleteTaskRecord(Task task) {
        asyncExecute(() -> mDatabaseRepository.deleteTaskRecord(task.parseToRecord()));
    }

    public void onTaskProgressUpdateById(Task task) {
        asyncExecute(() -> mDatabaseRepository.updateTaskRecord(task.getTaskId(), task.getTaskCurrentSizeJson()));
    }

    public void onTaskDownloadError(Task task) {
        asyncExecute(() -> mDatabaseRepository.updateTaskRecord(task.parseToRecord()));
    }

    /**
     * 加载本地记录监听
     *
     * @author wsb
     */
    public interface LoadLocalTaskListener {
        /**
         * 本地存在往期任务
         *
         * @param taskMap 往期任务内容
         */
        void localTaskExist(Map<Integer, Task> taskMap);
    }


}
