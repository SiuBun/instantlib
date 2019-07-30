package com.baselib.instant.alarm

/**
 * 闹钟吊起响应对象
 *
 * @author wsb
 * */
interface OnAlarmListener {
    /**
     * 此方法在主线程回调
     * @param alarmId 闹钟对应的id
     */
    fun onAlarm(alarmId:Int)
}