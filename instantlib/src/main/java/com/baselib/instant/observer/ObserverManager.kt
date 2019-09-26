package com.baselib.instant.observer

import android.content.Context

import com.baselib.instant.manager.IManager
import com.baselib.instant.observer.observer.AppChangeObserver
import com.baselib.instant.observer.observer.BaseObserver
import com.baselib.instant.observer.observer.NetStateObserver
import com.baselib.instant.util.LogUtils

import java.util.HashMap

/**
 * 观察者管理对象
 *
 * @author wsb
 */
class ObserverManager : IManager {
    private var mObserverMap: MutableMap<String, IObserver<*>> = HashMap()

    override fun onManagerDetach() {
        for (name in mObserverMap.keys) {
            mObserverMap[name]?.onObserverDetach()
        }
        mObserverMap.clear()
    }

    fun <T : IObserver<*>> getObserver(context: Context, name: String): T? {
        return if (null == mObserverMap[name]) {
            createObserver(context, name).takeIf { it != null }?.also {
                mObserverMap[name] = it
            } as T?
        } else {
            mObserverMap[name] as T
        }
    }

    private fun createObserver(context: Context, name: String): IObserver<*>? {
        return when (name) {
            APP_CHANGE_OBSERVER_NAME -> AppChangeObserver(context)
            NET_STATE_OBSERVER_NAME -> NetStateObserver(context)
            else -> null
        }
    }

    companion object {
        const val APP_CHANGE_OBSERVER_NAME = "AppChangeObserver"
        const val NET_STATE_OBSERVER_NAME = "NetStateObserver"
    }
}
