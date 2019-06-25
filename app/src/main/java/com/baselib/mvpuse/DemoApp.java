package com.baselib.mvpuse;

import android.app.Application;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.net.NetworkManager;
import com.baselib.instant.net.client.OkHttpNetClient;
import com.baselib.instant.util.LogUtils;

public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.setLogSwitch(BuildConfig.DEBUG);
        GlobalManager.initNetManager(
                new NetworkManager.Builder()
                        .setRetryCount(1)
                        .setTimeOut(10)
                        .setCacheAvailableTime(10)
                        .setCacheFile(getCacheDir())
                        .setClient(OkHttpNetClient.build())
                        .build()
        );
    }
}
