package com.baselib.instant.alarm

import android.content.Context

/**
 * 自定义闹钟管理器，管理多个闹钟（多个SDK同时使用时，可以各自管理自己的闹钟）
 *
 * 带上下文参数单例使用用双重检查的方式实现，对上下文持有只持有应用级别上下文
 *
 * @author wsb
 */
class CustomAlarmManager(context: Context) {

    private var mApplicationContext = context.applicationContext
    private var mAlarms = HashMap<String, CustomAlarm>()

    companion object {
        const val ALARM_ACTION_MIDDLE = ".instant.action.alarm."

        @Volatile
        private var instance: CustomAlarmManager? = null

        @JvmStatic
        fun getProvider(context: Context): CustomAlarmManager = instance ?: synchronized(this) {
            instance ?: CustomAlarmManager(context).also { instance = it }
        }
    }

    /**
     * 获取闹钟
     * @param moduleName 模块名称(每个SDK有一个唯一的模块名称，形如"tokencoin", "chargelocker"等)
     * @return 模块对应的闹钟对象
     */
    fun getAlarm(moduleName: String?): CustomAlarm? {
        var customAlarm: CustomAlarm? = null
        /*if (!moduleName.isNullOrEmpty()) {
            customAlarm ?: synchronized(mAlarms) {
                customAlarm
                        ?: CustomAlarm(mApplicationContext, mApplicationContext.packageName + ALARM_ACTION_MIDDLE + moduleName).also {
                            customAlarm = it
                            mAlarms[moduleName] = it
                        }
            }
        }*/

        customAlarm = moduleName.takeUnless { it.isNullOrEmpty() }?.let { name ->
            customAlarm ?: synchronized(mAlarms) {
                customAlarm
                        ?: CustomAlarm(mApplicationContext, mApplicationContext.packageName + ALARM_ACTION_MIDDLE + name).also { alarm ->
                            customAlarm = alarm
                            mAlarms[name] = alarm
                        }
            }
        }
        return customAlarm
    }

}