package com.baselib.instant.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;

/**
 * 应用（安装卸载事件）监听器
 *
 * @author wsb
 */
public class AppChangeObserver extends BaseObserver<AppChangeObserver.OnAppChangedListener>{

    public AppChangeObserver(Context context) {
        super(context);
    }

    @Override
    protected IntentFilter getIntentFilter() {
        IntentFilter appChangedFilter = new IntentFilter();
        //状态监听：程序安装/卸载
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appChangedFilter.addDataScheme("package");
        return appChangedFilter;
    }

    @Override
    protected BroadcastReceiver getReceiver() {
        return new AppChangedReceiver(this::onReceive);
    }


    private void notifyAppUninstalled(String pkgName) {
        List<OnAppChangedListener> listeners = getListenersCopy();
        for (OnAppChangedListener listener : listeners) {
            if (listener != null) {
                listener.onAppUninstalled(pkgName);
            }
        }
    }

    private void notifyAppReplaced(String pkgName) {
        List<OnAppChangedListener> listeners = getListenersCopy();
        for (OnAppChangedListener listener : listeners) {
            if (listener != null) {
                listener.onAppReplaced(pkgName);
            }
        }
    }

    private void notifyAppInstalled(String pkgName) {
        List<OnAppChangedListener> listeners = getListenersCopy();
        for (OnAppChangedListener listener : listeners) {
            if (listener != null) {
                listener.onAppInstalled(pkgName);
            }
        }
    }

    /**
     * 观察者接收到指定广播的响应
     *
     * @param action see{@link com.baselib.instant.observer.AppChangedReceiver.IAppChangedRegister#onReceiveChange(String, String)}
     * @param pkgName see{@link com.baselib.instant.observer.AppChangedReceiver.IAppChangedRegister#onReceiveChange(String, String)}
     */
    private void onReceive(String action, String pkgName) {
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            notifyAppUninstalled(pkgName);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            notifyAppReplaced(pkgName);
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            notifyAppInstalled(pkgName);
        }
    }

    /**
     * 程序安装卸载事件的监听
     *
     * @author wsb
     */
    public interface OnAppChangedListener extends ISubscriber {

        /**
         * 应用安装
         *
         * @param pkgName 包名
         */
        void onAppInstalled(String pkgName);

        /**
         * 应用卸载
         *
         * @param pkgName 包名
         */
        void onAppUninstalled(String pkgName);

        /**
         * 应用覆盖安装
         *
         * @param pkgName 包名
         */
        void onAppReplaced(String pkgName);
    }


}
