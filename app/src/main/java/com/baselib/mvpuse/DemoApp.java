package com.baselib.mvpuse;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.baselib.instant.breakpoint.BreakPointHelper;
import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.net.NetworkManager;
import com.baselib.instant.net.client.OkHttpNetClient;
import com.baselib.instant.repository.provider.sp.MultiProcessSharedPreferences;
import com.baselib.instant.util.LogUtils;
import com.baselib.use.BuildConfig;

public class DemoApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setDebugMode(BuildConfig.DEBUG);
        GlobalManager.Companion.initNetManager(
                new NetworkManager.Builder()
                        .setRetryCount(1)
                        .setTimeOut(10)
                        .setCacheAvailableTime(10)
                        .setCacheFile(getCacheDir())
                        .setClient(OkHttpNetClient.build())
                        .build()
        );

        BreakPointHelper.getInstance().attachApplication(this);

    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        LogUtils.e("onTerminate");
        super.onTerminate();
    }
    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        LogUtils.e("onLowMemory");
        super.onLowMemory();
    }
    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        LogUtils.e("onTrimMemory");
        super.onTrimMemory(level);
    }

    private void setDebugMode(boolean debug) {
        LogUtils.setLogSwitch(debug);
        MultiProcessSharedPreferences.DEBUG = debug;
    }
}
