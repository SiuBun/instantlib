package com.baselib.instant.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.io.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * 系统功能工具
 *
 * @author wsb
 */
class SystemUtil {
    companion object {
        const val MARSHMALLOW_MAC_ADDRESS = "02:00:00:00:00:00"
        const val FILE_ADDRESS_MAC = "/sys/class/net/wlan0/address"

        /**
         * 获取手机唯一标识
         *
         * @return 返回手机唯一标识，有可能返回空字符串
         */
        @SuppressLint("MissingPermission")
        fun getImei(context: Context): String {
            var imeiStr = ""
            val telephonyManager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                imeiStr = telephonyManager.deviceId
            } catch (e: Exception) {
                LogUtils.e("SystemUtil", "获取IMEI失败:$e")
            }
            return imeiStr
        }

        /**
         * @return 获取通信地址
         */
        fun getMacAddress(context: Context): String {
            var macAddress = ""
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            try {
                macAddress = wifiManager.connectionInfo!!.macAddress
            } catch (e: Exception) {
                LogUtils.e("SystemUtil", "获取Mac失败:$e")
            }
            return macAddress
        }

        fun getAdresseMAC(context: Context): String? {
            val wifiMan = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInf = wifiMan.connectionInfo

            var result: String? = MARSHMALLOW_MAC_ADDRESS
            if (wifiInf != null && MARSHMALLOW_MAC_ADDRESS == wifiInf.macAddress) {
                try {
                    result = getAddressMacByInterface()
                    if (result == null) {
                        result = getAddressMacByFile(wifiMan)
                    }
                } catch (e: Exception) {
                    Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ")
                }
            } else {
                result = if (wifiInf != null && wifiInf.macAddress != null) {
                    wifiInf.macAddress
                } else {
                    ""
                }
            }
            return result
        }

        private fun getAddressMacByInterface(): String? {
            var addressMac: String? = null

            try {
                val all = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if ("wlan0" == nif.name) {
                        val addressByte = nif.hardwareAddress
                        addressMac = if (addressMac == null) {
                            ""
                        } else {
                            val res1 = StringBuilder()
                            for (b in addressByte) {
                                res1.append(String.format("%02X:", b))
                            }
                            if (res1.isNotEmpty()) {
                                res1.deleteCharAt(res1.length - 1)
                            }
                            res1.toString()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC :$e")
            }

            return addressMac
        }

        @Throws(Exception::class)
        private fun getAddressMacByFile(wifiMan: WifiManager): String {
            val ret: String
            val wifiState = wifiMan.wifiState

            wifiMan.isWifiEnabled = true
            val fl = File(FILE_ADDRESS_MAC)
            val fin = FileInputStream(fl)
            ret = crunchifyGetStringFromStream(fin)
            fin.close()

            val enabled = WifiManager.WIFI_STATE_ENABLED == wifiState
            wifiMan.isWifiEnabled = enabled
            return ret
        }

        @Throws(IOException::class)
        private fun crunchifyGetStringFromStream(crunchifyStream: InputStream?): String {
            if (crunchifyStream != null) {
                val crunchifyWriter = StringWriter()

                val crunchifyBuffer = CharArray(2048)
                try {
                    val crunchifyReader = BufferedReader(InputStreamReader(crunchifyStream, "UTF-8"))
                    var counter = 0
                    while (counter != -1) {
                        crunchifyWriter.write(crunchifyBuffer, 0, counter)
                        counter = crunchifyReader.read(crunchifyBuffer)
                    }
                } finally {
                    crunchifyStream.close()
                }
                return crunchifyWriter.toString()
            } else {
                return "No Contents"
            }
        }

        /**
         * 获取手机的ip
         *
         * @return
         */
        fun getLocalIpAddress(): String {
            var localIpStr = ""
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf
                            .inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            localIpStr = inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }
            return localIpStr
        }

        /**
         * 使用 Map按key进行排序
         *
         * @param map
         * @return
         */
        fun sortMapByKey(map: Map<String, String>?): Map<String, String>? {
            return if (map.isNullOrEmpty()) {
                null
            } else {
                val sortMap = TreeMap<String, String>(Comparator { obj, s -> obj.compareTo(s) })
                sortMap.putAll(map)
                sortMap
            }
        }

        fun toast(context: Context?, message: String) {
            context?.let {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun byteToKb(bt: Int): Int {
            return bt / 1024
        }

        fun getTargetSdkVersion(context: Context): Int {
            var targetSdkVersion = -1
            try {
                val info = context.packageManager.getPackageInfo(context.packageName, 0)
                targetSdkVersion = info.applicationInfo.targetSdkVersion
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return targetSdkVersion
        }

        /**
         * 获取应用清单列表钟所有的meta节点
         *
         * @param context 上下文
         * @return 保存着所有meta节点
         */
        fun getMetaData(context: Context): Bundle? {
            var bundle: Bundle? = null
            try {
                val appInfo = context.packageManager.getApplicationInfo(context.packageName,
                        PackageManager.GET_META_DATA)

                if (appInfo?.metaData != null) {
                    bundle = appInfo.metaData
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                bundle = Bundle()
            }

            return bundle
        }

        /**
         * 获取密钥散列（FB需要用到）
         *
         * 不同签名文件打包出来的value是不同的
         *
         * @param context
         * @return 签名文件对应散列
         */
        fun getKeyHash(context: Context?): String? {
            if (context == null) {
                return null
            }

            try {
                val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    return Base64.encodeToString(md.digest(), Base64.DEFAULT)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return null
            } catch (e: NoSuchAlgorithmException) {
                return null
            }

            return null
        }
    }
}