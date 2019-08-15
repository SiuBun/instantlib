package com.baselib.instant.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.util.SparseArray
import com.baselib.instant.util.LogUtils

/**
 * 自定义闹钟，支持同时设置多个闹钟
 *
 * @author wsb
 */
class CustomAlarm(context: Context,
                  /** 闹钟行为 */
                  private val alarmAction: String) : TaskReceiver.TaskListener {

    override fun onReceiveTask(action: String?, alarmIdByReceive: Int) {
        if (alarmAction == action) {
            val alarmInfo = mPendingArray.get(alarmIdByReceive)
            alarmInfo?.also {
                //非重复闹钟，只使用一次
                if (alarmInfo.isRepeat) {
                    mAlarmManager.set(alarmInfo.alarmType, System.currentTimeMillis() + alarmInfo.repeatInterval, alarmInfo.pendingIntent)
                } else {
                    mPendingArray.remove(alarmIdByReceive)
                }
                alarmInfo.listener.onAlarm(alarmIdByReceive)
            }
        }
    }

    private val applicationContext: Context = context.applicationContext
    private val mPendingArray: SparseArray<AlarmInfo> = SparseArray()
    private var mAlarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var taskReceiver: TaskReceiver = TaskReceiver(this)


    init {
        registerReceiver()
    }

    /**
     * 注册广播监听器
     */
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(alarmAction)
        applicationContext.registerReceiver(taskReceiver, intentFilter)
    }

    /**
     * 清除所有已设置闹钟。
     */
    fun clear() {
        for (i in 0 until mPendingArray.size()) {
            val alarmInfo = mPendingArray.valueAt(i)
            mAlarmManager.cancel(alarmInfo.pendingIntent)
        }
    }

    /**
     * 延时triggerInterval后启动，一次性闹钟
     * @param alarmId 闹钟ID， 外部自己定义，要保证不重复且>0
     * @param triggerDelay 延时
     * @param isWakeup 手机睡眠时是否仍唤醒执行闹钟
     * @param listener 闹钟监听
     */
    fun alarmOneTime(alarmId: Int, triggerDelay: Long, isWakeup: Boolean, listener: OnAlarmListener?) {
        LogUtils.i(String.format("[CustomAlarm::alarm] alarmId:%d, triggerInterval:%d", alarmId, triggerDelay))
        if (null == listener) {
            throw IllegalArgumentException("参数错误！listener不能为null")
        }
        val intent = Intent(alarmAction)
        intent.putExtra(KEY_ALARMID, alarmId)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val triggerAtTime = System.currentTimeMillis() + triggerDelay
        val alarmType = if (isWakeup) AlarmManager.RTC_WAKEUP else AlarmManager.RTC
        mAlarmManager.set(alarmType, triggerAtTime, pendingIntent)

        mPendingArray.put(alarmId, AlarmInfo(pendingIntent, listener, false, 0, 0))
    }

    /**
     * 延时triggerInterval后启动，周期性闹钟
     * @param alarmId 闹钟ID， 外部自己定义，要保证不重复且>0
     * @param triggerDelay 第一次启动延时
     * @param interval 周期时长
     * @param isWakeup 手机睡眠时是否仍唤醒执行闹钟
     * @param listener 闹钟监听
     */
    fun alarmRepeat(alarmId: Int, triggerDelay: Long, interval: Long, isWakeup: Boolean, listener: OnAlarmListener?) {
        LogUtils.i("matt", String.format("[CustomAlarm::alarmRepeat] alarmId:%d, triggerInterval:%d<>interval:%d", alarmId, triggerDelay, interval))
        if (null == listener) {
            throw IllegalArgumentException("参数错误！listener不能为null")
        }

        val intent = Intent(alarmAction)
        intent.putExtra(KEY_ALARMID, alarmId)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val triggerAtTime = System.currentTimeMillis() + triggerDelay
        val alarmType = if (isWakeup) AlarmManager.RTC_WAKEUP else AlarmManager.RTC
        //            mAlarmManager.setRepeating(alarmType, triggerAtTime, interval, pendingIntent);
        mAlarmManager.set(alarmType, triggerAtTime, pendingIntent)

        mPendingArray.put(alarmId, AlarmInfo(pendingIntent, listener, true, alarmType, interval))
    }

    /**
     * 延时triggerInterval后启动，周期性闹钟.
     * <br></br>此方法会缓存每次闹钟执行的时间到sharepreference，以sp的时间作为计时的起点。第n>1次闹钟，忽视原triggerDelay。
     * <br></br>如一个闹钟，interval为8小时，1点闹钟触发后，进程被杀死，6点应用重启又再设置同一个(id)闹钟，则9点闹钟即被触发。
     * <br></br>要与 [.saveTriggerTime]方法配套使用
     * @param alarmId 闹钟ID， 外部自己定义，要保证不重复且>0
     * @param triggerDelay 第一次启动延时
     * @param interval 周期时长
     * @param isWakeup 手机睡眠时是否仍唤醒执行闹钟
     * @param listener 闹钟监听
     */
    fun alarmRepeatInRealTime(sp: SharedPreferences, alarmId: Int, triggerDelay: Long, interval: Long, isWakeup: Boolean, listener: OnAlarmListener) {
        var triggerDelay = triggerDelay
        LogUtils.i("matt", String.format("[CustomAlarm::alarmRepeatInRealTime] alarmId:%d, triggerInterval:%d<>interval:%d", alarmId, triggerDelay, interval))

        // 获取上次触发闹钟的时间
        val lastTime = sp.getLong(getLastTimeKey(alarmId), -1)
        if ((-1).toLong() != lastTime) {
            //已经执行过闹钟，计算triggerDelay
            val passTime = System.currentTimeMillis() - lastTime
            triggerDelay = interval - passTime
            //已经超时，需要即刻触发闹钟
            if (triggerDelay < 0) {
                triggerDelay = 0
            }
        }

        alarmRepeat(alarmId, triggerDelay, interval, isWakeup, listener)
    }

    /**
     * 保存闹钟触发时间
     *
     * 要与 [.alarmRepeatInRealTime]方法配套使用
     */
    fun saveTriggerTime(sp: SharedPreferences, alarmId: Int) {
        sp.edit().putLong(getLastTimeKey(alarmId), System.currentTimeMillis()).apply()
    }

    /**
     * 取消闹钟
     * @param alarmId 闹钟ID， 外部自己定义，要保证不重复且>0
     */
    fun cancelAarm(alarmId: Int) {
        val alarmInfo = mPendingArray.get(alarmId) ?: return
        mAlarmManager.cancel(alarmInfo.pendingIntent)
        mPendingArray.remove(alarmId)
    }

    private fun getLastTimeKey(alarmId: Int): String {
        return alarmAction.hashCode().toString() + "_" + alarmId
    }

    fun isAlarmActive(alarmId: Int): Boolean {
        return mPendingArray.get(alarmId) != null
    }

    companion object {
        /**无效闹钟ID */
        const val ALARMID_INVALID = -1
        /**
         * 闹钟ID的key
         */
        const val KEY_ALARMID = "alarmId"
    }


}