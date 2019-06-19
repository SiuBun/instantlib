package com.baselib.mvpuse;

import android.app.Application;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.net.NetworkManager;
import com.baselib.instant.net.client.OkHttpNetClient;

public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GlobalManager.initNetManager(
                new NetworkManager.Builder()
                        .setRetryCount(1)
                        .setTimeOut(10)
                        .setClient(OkHttpNetClient.build())
                        .build()
        );
    }
}
