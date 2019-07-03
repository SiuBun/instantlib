package com.baselib.instant.observer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 系统事件监听
 *
 * @author wsb
 */
class SdCardMountReceiver(private var sdCardMountRegister: ISdCardMountRegister?) : BroadcastReceiver() {

    interface ISdCardMountRegister {
        /**
         * sd卡变化回调
         *
         * @param action 回调信息
         */
        fun onReceive(action: String?)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        sdCardMountRegister?.onReceive(intent.action)
    }
}