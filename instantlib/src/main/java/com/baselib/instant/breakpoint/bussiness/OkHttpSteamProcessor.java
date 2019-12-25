package com.baselib.instant.breakpoint.bussiness;

import android.support.annotation.WorkerThread;

import com.baselib.instant.breakpoint.operate.FileStreamListener;
import com.baselib.instant.breakpoint.operate.RangeDownloadListener;
import com.baselib.instant.breakpoint.operate.StreamProcessor;
import com.baselib.instant.breakpoint.utils.BreakPointConst;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp形式获取文件流对象
 *
 * @author wsb
 */
public class OkHttpSteamProcessor implements StreamProcessor {

    private OkHttpClient mOkHttpClient;

    @Override
    @WorkerThread
    public void getCompleteFileStream(String url, FileStreamListener streamListener) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                streamListener.getFileStreamFail(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                final ResponseBody body = response.body();
                if (response.code() == BreakPointConst.REQ_SUCCESS && null != body) {
                    final long contentLength = body.contentLength();
                    final InputStream byteStream = body.byteStream();
                    body.close();
                    streamListener.getFileStreamSuccess(contentLength, byteStream);
                } else {
                    streamListener.getFileStreamFail("该链接请求码为" + response.code() + "或者response.body()为空");
                }
            }
        });
    }

    @Override
    @WorkerThread
    public void downloadRangeFile(String url, File file, long startIndex, long endIndex, RangeDownloadListener downloadListener) throws IOException {
        Request request = new Request.Builder()
                .header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();

        final Response response = getHttpClient().newCall(request).execute();
        final ResponseBody body = response.body();
        if (response.code() == BreakPointConst.REQ_RANGE_SUCCESS && null != body) {
            final InputStream is = body.byteStream();
            // 获取前面已创建的文件.
            RandomAccessFile tmpAccessFile = new RandomAccessFile(file, "rw");
            // 文件写入的开始位置.
            tmpAccessFile.seek(startIndex);

            byte[] buffer = new byte[1024 << 2];
            int length = -1;
            // 记录本次请求所下载文件的长度
            int currentDownloadLength = 0;
//            当前分段文件的写入的位置
            long currentRangeFileIndex = 0;

//            long millis = SystemClock.uptimeMillis();
            while ((length = is.read(buffer)) > 0) {
                tmpAccessFile.write(buffer, 0, length);
//                更新本次已下载长度
                currentDownloadLength += length;
//                更新当前分段文件的已下载的长度,即文件实际下载起点加已下载长度
                currentRangeFileIndex = startIndex + currentDownloadLength;
                downloadListener.updateRangeProgress(currentRangeFileIndex);

//                if (SystemClock.uptimeMillis() - millis >= 1000) {
//                    millis = SystemClock.uptimeMillis();
//                }
            }

//            分段任务下载完成
            downloadListener.rangeDownloadFinish(currentDownloadLength, currentRangeFileIndex);

        } else {
            downloadListener.rangeDownloadFail("该链接请求码为" + response.code() + "或者response.body()为空");
        }
    }

    private OkHttpClient getHttpClient() {
        if (null == mOkHttpClient) {
            mOkHttpClient = new OkHttpClient.Builder()
//                    .connectTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                    .readTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                    .callTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }


}
