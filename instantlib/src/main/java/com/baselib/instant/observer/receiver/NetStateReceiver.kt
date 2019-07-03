package com.baselib.instant.observer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.baselib.instant.util.NetUtil

/**
 * 系统事件监听
 *
 * @author wsb
 */
class NetStateReceiver constructor(private val netStateRegister: INetStateRegister?) : BroadcastReceiver() {
    interface INetStateRegister {
        /**
         * 网络状态变化回调
         *
         * @param netWorkAvailable 网络是否可用
         * @param wifiEnable       wifi是否可用
         */
        fun onReceive(netWorkAvailable: Boolean, wifiEnable: Boolean)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        netStateRegister?.onReceive(NetUtil.isNetWorkAvailable(context), NetUtil.isWifiEnable(context))

//			 Log.i("NetStateReceiver", "[NetStateReceiver#onReceive] net:" + NetUtil.isNetWorkAvailable(context) + ", wifi:" + NetUtil.isWifiEnable(context) + ", action:" + intent.getAction());
//
//			 String action = intent.getAction();
//			 if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//				 /**
//				  * WifiManager.WIFI_STATE_CHANGED_ACTION， 这个监听wifi的打开与关闭，与wifi的连接无关
//				  * WifiManager.NETWORK_STATE_CHANGED_ACTION， 这个监听wifi的连接状态
//				  */
//				 NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//				 if (networkInfo != null) {
//					 sInstance.setWifiState(networkInfo.isConnected());
//				 }
//			 } else if (IS_OBSERVER_GENERAL_NETSTATE && ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
//				 //这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
//				 //最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
//				 // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
//				 sInstance.setNetState(NetUtil.isNetWorkAvailable(context));
//			 }
    }

}