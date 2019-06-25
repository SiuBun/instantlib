package com.baselib.instant.net.base;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Response;

/**
 * 主线程回调处理
 *
 * @author wsb
 */
public class CallbackHandler extends Handler {

    public CallbackHandler() {
        super(Looper.getMainLooper());
    }

    public void reqBefore(final IHttpStateCallback stateCallback, final String url) {
        stateCallback.reqBefore(url);
    }

    public void reqStart(final IHttpStateCallback stateCallback) {
        post(() -> stateCallback.reqStart());
    }

    public void reqFinish(final IHttpStateCallback stateCallback) {
        post(() -> stateCallback.reqFinish());
    }

    public void reqError(final IHttpStateCallback stateCallback, final IOException e) {
        post(() -> stateCallback.reqError(e));
    }

    public void reqFail(final IHttpStateCallback stateCallback, final String failInfo) {
        post(() -> stateCallback.reqFail(failInfo));
    }

    public void reqSuccess(final IHttpStateCallback stateCallback, final Response response) {
        post(() -> stateCallback.reqSuccess(response));
    }
}
