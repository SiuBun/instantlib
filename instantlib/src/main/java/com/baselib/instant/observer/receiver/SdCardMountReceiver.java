package com.baselib.instant.observer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 系统事件监听
 *
 * @author wsb
 */
public class SdCardMountReceiver extends BroadcastReceiver {
    ISdCardMountRegister mSdCardMountRegister;

    public SdCardMountReceiver(ISdCardMountRegister cardMountRegister) {
        this.mSdCardMountRegister = cardMountRegister;
    }

    public interface ISdCardMountRegister {
        /**
         * sd卡变化回调
         *
         * @param action 回调信息
         */
        void onReceive(String action);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSdCardMountRegister == null) {
            //只有监听器被初始化过，才回调
            return;
        }
        mSdCardMountRegister.onReceive(intent.getAction());
    }
}