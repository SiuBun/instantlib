package com.baselib.instant.observer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.baselib.instant.observer.ISubscriber

/**
 * 系统应用事件监听
 *
 * @author wsb
 */
class AppChangedReceiver(private var changedRegister: IAppChangedRegister?) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (null != intent) {
            val action = intent.action
            val packageName = intent.data!!.schemeSpecificPart
            when(action){
                Intent.ACTION_PACKAGE_REMOVED->changedRegister?.onReceiveChange(action, packageName)
                Intent.ACTION_PACKAGE_REPLACED->changedRegister?.onReceiveChange(action, packageName)
                Intent.ACTION_PACKAGE_ADDED->changedRegister?.onReceiveChange(action, packageName)
            }
        }
    }

    interface IAppChangedRegister : ISubscriber {

        /**
         * 接收变化回调
         *
         * @param action      具体行为
         * @param packageName 发生行为的包名
         */
        fun onReceiveChange(action: String, packageName: String)
    }
}