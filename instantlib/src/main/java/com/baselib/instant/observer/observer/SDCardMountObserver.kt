package com.baselib.instant.observer.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import com.baselib.instant.observer.ISubscriber
import com.baselib.instant.observer.receiver.SdCardMountReceiver
import com.baselib.instant.util.Machine

/**
 * 应用（安装卸载事件）监听器
 *
 * @author wsb
 */
class SDCardMountObserver private constructor(context: Context) : BaseObserver<SDCardMountObserver.IOnSdStatusChangedListener>(context) {

    override fun getIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        intentFilter.addDataScheme("file")
        return intentFilter
    }

    override fun getReceiver(): BroadcastReceiver {
        return SdCardMountReceiver(object :SdCardMountReceiver.ISdCardMountRegister {
            override fun onReceive(action: String?) {
                action?.let { onReceiveAction(it) }
            }
        })
    }

    /**
     * SD卡状态变化
     *
     * @param isAvailable
     */
    private fun notifySdCardAvailableChanged(isAvailable: Boolean) {
        val listeners = listenersCopy
        for (listener in listeners) {
            listener.onSdCardAvailableChanged(isAvailable)
        }
    }

    /**
     * 接收响应
     *
     * @param action 具体行为
     */
    private fun onReceiveAction(action: String) {
        synchronized(lock) {
            if (Intent.ACTION_MEDIA_MOUNTED == action) {
                notifySdCardAvailableChanged(true)
            } else if (Intent.ACTION_MEDIA_UNMOUNTED == action) {
                notifySdCardAvailableChanged(false)
            }
        }
    }

    /**
     * 程序安装卸载事件的监听
     *
     * @author wsb
     */
    interface IOnSdStatusChangedListener : ISubscriber {

        /**
         * SD卡可用变化
         *
         * @param isAvailable SD卡是否可用
         */
        fun onSdCardAvailableChanged(isAvailable: Boolean)
    }

    companion object {

        /**
         * 获取sd卡是否可用
         *
         * @return
         */
        val isSDCardAvailable: Boolean
            get() = Machine.isSDCardExist()
    }


}
