package com.baselib.instant.observer.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.baselib.instant.observer.ISubscriber;
import com.baselib.instant.observer.receiver.NetStateReceiver;
import com.baselib.instant.util.NetUtil;

import java.util.List;

/**
 * 网络状态监听器
 *
 * @author wsb
 */
public class NetStateObserver extends BaseObserver<NetStateObserver.OnNetStateChangeListener> {
    /**
     * 是否监听普通网络状态变化（false只监听wifi状态变化)
     */
    private static final boolean IS_OBSERVER_GENERAL_NETSTATE = true;

    private boolean mIsNetAvailable;
    private boolean mIsWifiConnected;


    private NetStateObserver(Context context) {
        super(context);
        mIsNetAvailable = NetUtil.isNetWorkAvailable(context);
        mIsWifiConnected = NetUtil.isWifiEnable(context);
    }

    @Override
    protected IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (IS_OBSERVER_GENERAL_NETSTATE) {
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        }
        return filter;
    }

    @Override
    protected BroadcastReceiver getReceiver() {
        return new NetStateReceiver(this::onReceive);
    }

    /**
     * 观察者接收到指定广播的响应
     *
     * @param netAvailable  see{@link NetStateReceiver.INetStateRegister#onReceive(boolean, boolean)}
     * @param wifiAvailable see{@link NetStateReceiver.INetStateRegister#onReceive(boolean, boolean)}
     */
    private void onReceive(boolean netAvailable, boolean wifiAvailable) {
        setNetState(netAvailable);
        setWifiState(wifiAvailable);
    }


    /**
     * 网络状态变化
     *
     * @param isAvailable
     */
    private void notifyNetStateChanged(boolean isAvailable) {
        List<OnNetStateChangeListener> listeners = getListenersCopy();
        for (OnNetStateChangeListener listener : listeners) {
            if (listener != null) {
                listener.onNetStateChanged(isAvailable);
            }
        }
    }

    /**
     * Wifi状态变化
     *
     * @param isConnected
     */
    private void notifyWifiStateChanged(boolean isConnected) {
        List<OnNetStateChangeListener> listeners = getListenersCopy();
        for (OnNetStateChangeListener listener : listeners) {
            if (listener != null) {
                listener.onWifiStateChanged(isConnected);
            }
        }
    }

    private void setNetState(boolean isAvailable) {
        if (mIsNetAvailable == isAvailable) {
            return;
        }
        mIsNetAvailable = isAvailable;
        notifyNetStateChanged(isAvailable);
    }

    private void setWifiState(boolean isConnected) {
        if (mIsWifiConnected == isConnected) {
            return;
        }
        mIsWifiConnected = isConnected;
        notifyWifiStateChanged(isConnected);
    }

    /**
     * 网络状态监听
     *
     * @author wsb
     */
    public interface OnNetStateChangeListener extends ISubscriber {
        /**
         * 网络状态变化
         *
         * @param isAvailable true代表可用
         */
        void onNetStateChanged(boolean isAvailable);

        /**
         * Wifi状态变化
         *
         * @param isConnected true代表可用
         */
        void onWifiStateChanged(boolean isConnected);
    }


}
