package com.baselib.instant.net.client;

import com.baselib.instant.net.base.CallbackHandler;
import com.baselib.instant.net.base.IHttpStateCallback;
import com.baselib.instant.net.base.INetClient;
import com.baselib.instant.net.base.NetConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * okhttp请求方案
 *
 * @author wsb
 */
public class OkHttpNetClient implements INetClient {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    private OkHttpClient mClient;
    private CallbackHandler mCallbackHandler;

    public OkHttpNetClient() {
        mCallbackHandler = new CallbackHandler();
    }

    public static OkHttpNetClient build() {
        return new OkHttpNetClient();
    }

    @Override
    public void setConfig(NetConfig config) {
        int timeout = config.getTimeout();
        mClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .callTimeout(timeout, TimeUnit.SECONDS)
                .build();

    }

    @Override
    public void reqGet(String url, Map<String, Object> params, final IHttpStateCallback stateCallback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        enqueue(stateCallback, request);
    }

    private void enqueue(final IHttpStateCallback stateCallback, Request request) {
        mCallbackHandler.reqBefore(stateCallback,request.url().toString());
        mCallbackHandler.reqStart(stateCallback);
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mCallbackHandler.reqError(stateCallback,e);
                mCallbackHandler.reqFinish(stateCallback);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (null == body){
                        mCallbackHandler.reqFail(stateCallback,"body is null");
                    }else {
                        mCallbackHandler.reqSuccess(stateCallback,body.toString());
                    }
                } else {
                    mCallbackHandler.reqFail(stateCallback,"response fail");
                }

                mCallbackHandler.reqFinish(stateCallback);
            }
        });
    }

    @Override
    public void reqPost(String url, Map<String, Object> params, IHttpStateCallback stateCallback) {
        
    }

    @Override
    public void detach() {
        mCallbackHandler.removeCallbacksAndMessages(null);
        mClient = null;
    }


}
