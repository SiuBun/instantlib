package com.baselib.instant.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import java.net.NetworkInterface
import java.net.SocketException

/**
 * 网络工具类
 *
 * @author wsb
 */
class NetUtil {
    companion object {

        private const val NETWORK_STATE_UNKNOWN = 0;
        private const val NETWORK_STATE_WIFI = 1;
        private const val NETWORK_STATE_MOBILE_2G = 2;
        private const val NETWORK_STATE_MOBILE_3G4G = 3;

        /**
         * 检测网络是否可用
         *
         * @param context 上下文
         * @return true代表网络可用
         */
        fun isNetWorkAvailable(context: Context?): Boolean {
            var available = false
            val manager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            /**
             * 防止这个crash
             * Caused by: android.os.DeadSystemException
            ... 11 more
            android.os.DeadSystemException
            at android.net.ConnectivityManager.getActiveNetworkInfo(ConnectivityManager.java:876)

             */
            try {
                val networkInfo = manager.activeNetworkInfo
                if (networkInfo.isAvailable) {
                    available = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return available
        }

        /**
         * 获取当前网络状态
         *
         * unknown,wifi,GPRS,3G,4G
         *
         * @param context 上下文
         * @return 0,1,2,3,3对应unknown,wifi,GPRS,3G,4G
         */
        fun getNetworkType(context: Context): Int {
            var networkState = NETWORK_STATE_UNKNOWN
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                val networkinfo = manager.activeNetworkInfo
                if (networkinfo.type == ConnectivityManager.TYPE_WIFI) {
                    networkState = NETWORK_STATE_WIFI
                } else {
                    if (networkinfo.type == ConnectivityManager.TYPE_MOBILE) {
                        networkState = when (networkinfo.subtype) {
                            TelephonyManager.NETWORK_TYPE_1xRTT -> NETWORK_STATE_MOBILE_2G
                            TelephonyManager.NETWORK_TYPE_CDMA -> NETWORK_STATE_MOBILE_2G
                            TelephonyManager.NETWORK_TYPE_GPRS -> NETWORK_STATE_MOBILE_2G
                            TelephonyManager.NETWORK_TYPE_IDEN -> NETWORK_STATE_MOBILE_2G

                            TelephonyManager.NETWORK_TYPE_EVDO_0 -> NETWORK_STATE_MOBILE_3G4G
                            TelephonyManager.NETWORK_TYPE_EVDO_A -> NETWORK_STATE_MOBILE_3G4G
                            TelephonyManager.NETWORK_TYPE_HSDPA -> NETWORK_STATE_MOBILE_3G4G
                            TelephonyManager.NETWORK_TYPE_HSPA -> NETWORK_STATE_MOBILE_3G4G
                            TelephonyManager.NETWORK_TYPE_HSUPA -> NETWORK_STATE_MOBILE_3G4G
                            TelephonyManager.NETWORK_TYPE_UMTS -> NETWORK_STATE_MOBILE_3G4G

                            else -> NETWORK_STATE_UNKNOWN
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return networkState
        }

        /**
         * 获取本地IP函数
         *
         * @return ip函数字符串
         */
        fun getLocalIPAddress(): String {
            var address = ""
            try {
                val enumeration = NetworkInterface.getNetworkInterfaces()
                while (enumeration.hasMoreElements()) {
                    val next = enumeration.nextElement()
                    val enumIPAddr = next.inetAddresses
                    while (enumIPAddr.hasMoreElements()) {
                        val inetAddress = enumIPAddr.nextElement()
                        //如果不是回环地址
                        if (!inetAddress.isLoopbackAddress) {
                            //直接返回本地IP地址
                            address = inetAddress.hostAddress
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
                address = null.toString()
            }
            return address
        }


        /**
         * 检测WIFI是否可用
         *
         * @param context
         * @return
         */
        fun isWifiEnable(context: Context?): Boolean {
            var result = false
            if (context != null) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var networkInfo: NetworkInfo? = null
                try {
                    networkInfo = connectivityManager.activeNetworkInfo
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (networkInfo != null && networkInfo.isConnected
                        && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    result = true
                }
            }
            return result
        }
    }

}