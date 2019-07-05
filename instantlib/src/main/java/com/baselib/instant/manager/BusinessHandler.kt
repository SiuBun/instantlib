package com.baselib.instant.manager


import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * 事务处理类
 *
 *
 * 项目中基类实例化,故项目中维护一个Handler类,剩余扩展交给其他[IHandlerMsgListener]处理
 *
 * @author wsb
 */
class BusinessHandler(looper: Looper, private var listener: IHandlerMsgListener?) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        listener?.handleMessage(msg)
    }

    /**
     * 销毁阶段回调
     */
    fun onDestroy() {
        if (listener != null) {
            listener = null
        }
        removeCallbacksAndMessages(null)
    }

    /**
     * handler消息处理器回调
     */
    interface IHandlerMsgListener {
        /**
         * 处理消息
         * @param msg 收到的消息对象
         */
        fun handleMessage(msg: Message)
    }
}
