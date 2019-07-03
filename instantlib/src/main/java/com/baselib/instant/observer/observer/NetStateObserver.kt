package com.baselib.instant.observer.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.baselib.instant.observer.ISubscriber
import com.baselib.instant.observer.receiver.NetStateReceiver
import com.baselib.instant.util.NetUtil

/**
 * 网络状态监听器
 *
 * @author wsb
 */
class NetStateObserver(context: Context) : BaseObserver<NetStateObserver.OnNetStateChangeListener>(context) {

    private var wifiConnected: Boolean = NetUtil.isWifiEnable(context)
        set(value) {
            if (field != value) {
                field = value
                notifyNetStateChanged(value)
            }
        }

    private var netAvailable: Boolean = NetUtil.isNetWorkAvailable(context)
        set(value) {
            if (field != value) {
                field = value
                notifyWifiStateChanged(value)
            }
        }

    companion object {
        /**
         * 是否监听普通网络状态变化（false只监听wifi状态变化)
         */
        const val IS_OBSERVER_GENERAL_NETSTATE = true
    }

    override fun getReceiver(): BroadcastReceiver {
        return NetStateReceiver(object : NetStateReceiver.INetStateRegister {
            override fun onReceive(netWorkAvailable: Boolean, wifiEnable: Boolean) {
                onReceiveChange(netWorkAvailable, wifiEnable)
            }
        })
    }

    override fun getIntentFilter(): IntentFilter? {
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        if (IS_OBSERVER_GENERAL_NETSTATE) {
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        return filter
    }

    /**
     * 网络状态变化
     *
     * @param isAvailable
     */
    private fun notifyNetStateChanged(isAvailable: Boolean) {
        for (listener in listenersCopy) {
            listener.onNetStateChanged(isAvailable)
        }
    }

    /**
     * Wifi状态变化
     *
     * @param isConnected
     */
    private fun notifyWifiStateChanged(isConnected: Boolean) {
        for (listener in listenersCopy) {
            listener.onWifiStateChanged(isConnected)
        }
    }

    /**
     * 网络状态监听
     *
     * @author wsb
     */
    interface OnNetStateChangeListener : ISubscriber {
        /**
         * 网络状态变化
         *
         * @param isAvailable true代表可用
         */
        fun onNetStateChanged(isAvailable: Boolean)

        /**
         * Wifi状态变化
         *
         * @param isConnected true代表可用
         */
        fun onWifiStateChanged(isConnected: Boolean)
    }

    private fun onReceiveChange(netCanUse: Boolean, wifiCanUse: Boolean) {
        netAvailable = netCanUse
        wifiConnected = wifiCanUse
    }
}

