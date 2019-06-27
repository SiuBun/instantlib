package com.baselib.instant.observer.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baselib.instant.observer.ISubscriber;
import com.baselib.instant.observer.receiver.SdCardMountReceiver;
import com.baselib.instant.util.Machine;

import java.util.List;

/**
 * 应用（安装卸载事件）监听器
 *
 * @author wsb
 */
public class SDCardMountObserver extends BaseObserver<SDCardMountObserver.IOnSdStatusChangedListener> {

    private SDCardMountObserver(Context context) {
        super(context);
    }

    @Override
    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        return intentFilter;
    }

    @Override
    protected BroadcastReceiver getReceiver() {
        return new SdCardMountReceiver(this::onReceive);
    }

    /**
     * 获取sd卡是否可用
     *
     * @return
     */
    public static boolean isSDCardAvailable() {
        return Machine.isSDCardExist();
    }

    /**
     * SD卡状态变化
     *
     * @param isAvailable
     */
    private void notifySdCardAvailableChanged(boolean isAvailable) {
        List<IOnSdStatusChangedListener> listeners = getListenersCopy();
        for (IOnSdStatusChangedListener listener : listeners) {
            if (listener != null) {
                listener.onSdCardAvailableChanged(isAvailable);
            }
        }
    }

    /**
     * 接收响应
     *
     * @param action 具体行为
     */
    private void onReceive(String action) {

        synchronized (getLock()) {
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                notifySdCardAvailableChanged(true);
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                notifySdCardAvailableChanged(false);
            }
        }
    }

    /**
     * 程序安装卸载事件的监听
     *
     * @author wsb
     */
    public interface IOnSdStatusChangedListener extends ISubscriber {

        /**
         * SD卡可用变化
         *
         * @param isAvailable SD卡是否可用
         */
        void onSdCardAvailableChanged(boolean isAvailable);
    }


}
