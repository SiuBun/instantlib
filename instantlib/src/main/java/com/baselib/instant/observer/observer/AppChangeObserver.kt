package com.baselib.instant.observer.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import com.baselib.instant.observer.ISubscriber
import com.baselib.instant.observer.receiver.AppChangedReceiver

/**
 * 应用（安装卸载事件）监听器
 *
 * @author wsb
 */
class AppChangeObserver(context: Context) : BaseObserver<AppChangeObserver.OnAppChangedListener>(context) {

    override fun getIntentFilter(): IntentFilter? {
        val appChangedFilter = IntentFilter()
        //状态监听：程序安装/卸载
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        appChangedFilter.addDataScheme("package")
        return appChangedFilter
    }

    override fun getReceiver(): BroadcastReceiver {
        return AppChangedReceiver(object :AppChangedReceiver.IAppChangedRegister {
            override fun onReceiveChange(action: String, packageName: String) {
                onReceive(action, packageName)
            }
        })
    }


    private fun notifyAppUninstalled(pkgName: String) {
        for (listener in listenersCopy) {
            listener.onAppUninstalled(pkgName)
        }
    }

    private fun notifyAppReplaced(pkgName: String) {
        for (listener in listenersCopy) {
            listener.onAppReplaced(pkgName)
        }
    }

    private fun notifyAppInstalled(pkgName: String) {
        for (listener in listenersCopy) {
            listener.onAppInstalled(pkgName)
        }
    }

    /**
     * 观察者接收到指定广播的响应
     *
     * @param action see[AppChangedReceiver.IAppChangedRegister.onReceiveChange]
     * @param pkgName see[AppChangedReceiver.IAppChangedRegister.onReceiveChange]
     */
    private fun onReceive(action: String, pkgName: String) {
        when (action) {
            Intent.ACTION_PACKAGE_REMOVED -> notifyAppUninstalled(pkgName)
            Intent.ACTION_PACKAGE_REPLACED -> notifyAppReplaced(pkgName)
            Intent.ACTION_PACKAGE_ADDED -> notifyAppInstalled(pkgName)
        }
    }

    /**
     * 程序安装卸载事件的监听
     *
     * @author wsb
     */
    interface OnAppChangedListener : ISubscriber {

        /**
         * 应用安装
         *
         * @param pkgName 包名
         */
        fun onAppInstalled(pkgName: String)

        /**
         * 应用卸载
         *
         * @param pkgName 包名
         */
        fun onAppUninstalled(pkgName: String)

        /**
         * 应用覆盖安装
         *
         * @param pkgName 包名
         */
        fun onAppReplaced(pkgName: String)
    }


}
