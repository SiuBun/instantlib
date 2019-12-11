package com.baselib.instant.breakpoint;

import com.baselib.instant.breakpoint.utils.BreakPointConst;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
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
 * */
public class OkHttpSteamProcessor implements StreamProcessor {

    private OkHttpClient mOkHttpClient;

    @Override
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
                if (response.code()==BreakPointConst.REQ_SUCCESS && null!= body){
                    final long contentLength = body.contentLength();
                    final InputStream byteStream = body.byteStream();
                    body.close();
                    streamListener.getFileStreamSuccess(contentLength, byteStream);
                }else {
                    streamListener.getFileStreamFail("该链接请求码为"+response.code()+"或者response.body()为空");
                }
            }
        });
    }

    private OkHttpClient getHttpClient() {
        if (null == mOkHttpClient) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .callTimeout(BreakPointConst.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }



}
