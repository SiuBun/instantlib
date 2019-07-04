package com.baselib.instant.observer

import android.content.Context

import com.baselib.instant.manager.IManager
import com.baselib.instant.observer.observer.AppChangeObserver
import com.baselib.instant.util.LogUtils

import java.util.HashMap

/**
 * 观察者管理对象
 *
 * @author wsb
 */
class ObserverManager : IManager {
    private var mObserverMap: MutableMap<String, IObserver<*>> = HashMap()

    val appChangeObserver: AppChangeObserver?
        get() = getObserver(APP_CHANGE_OBSERVER_NAME, AppChangeObserver::class.java)

    override fun detach() {
        for (name in mObserverMap.keys) {
            mObserverMap[name]?.onDestroy()
        }
    }

    /**
     * 该方法内对观察者管理对象进行初始化
     *
     *
     * 后面get方法获取到的观察者对象不为空需要在此put进容器钟
     *
     * @param context 上下文
     */
    fun attach(context: Context) {
        mObserverMap[APP_CHANGE_OBSERVER_NAME] = AppChangeObserver(context)
    }

    private fun <T : IObserver<*>> getObserver(name: String, clz: Class<T>): T? {
        return if (mObserverMap[name] == null) {
            LogUtils.d("该观察者暂未被支持")
            null
        } else {
            mObserverMap[name] as T?
        }
    }

    companion object {
        private const val APP_CHANGE_OBSERVER_NAME = "AppChangeObserver"
    }
}
