package com.baselib.instant.alarm

import android.app.PendingIntent

/**
 * 闹钟对象
 *
 * @author wsb
 * */
class AlarmInfo constructor(val pendingIntent: PendingIntent, val listener: OnAlarmListener, val isRepeat:Boolean, val alarmType:Int, val repeatInterval:Long){
}