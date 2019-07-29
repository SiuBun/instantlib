package com.baselib.instant.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 任务广播接受者
 * @author wsb
 */
class TaskReceiver constructor(val taskListener: TaskListener) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val alarmId = it.getIntExtra(CustomAlarm.KEY_ALARMID, CustomAlarm.ALARMID_INVALID)
            taskListener.onReceiveTask(it.action, alarmId)
        }
    }

    /**
     * 任务监听
     *
     * @author wsb
     * */
    interface TaskListener {
        /**
         * 接收到广播对注册广播的对象进行通知
         *
         * @param action 广播行为
         * @param alarmIdByReceive 广播中携带的闹钟id
         * */
        fun onReceiveTask(action: String?, alarmIdByReceive: Int)
    }
}