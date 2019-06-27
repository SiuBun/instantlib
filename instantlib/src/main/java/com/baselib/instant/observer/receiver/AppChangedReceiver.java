package com.baselib.instant.observer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baselib.instant.observer.ISubscriber;

/**
 * 系统应用事件监听
 *
 * @author wsb
 */
public class AppChangedReceiver extends BroadcastReceiver {

    IAppChangedRegister mChangedRegister;

    public AppChangedReceiver(IAppChangedRegister appChangedRegister) {
        this.mChangedRegister = appChangedRegister;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mChangedRegister == null) {
            //只有监听器被初始化过，才回调
            return;
        }

        String action = intent.getAction();
        String packageName = intent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            mChangedRegister.onReceiveChange(action, packageName);
        }
    }



    public interface IAppChangedRegister extends ISubscriber {

        /**
         * 接收变化回调
         *
         * @param action      具体行为
         * @param packageName 发生行为的包名
         */
        void onReceiveChange(String action, String packageName);
    }
}