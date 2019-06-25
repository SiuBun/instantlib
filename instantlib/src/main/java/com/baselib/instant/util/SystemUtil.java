package com.baselib.instant.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 系统功能工具
 *
 * @author wsb
 */
public class SystemUtil {


    /**
     * 获取手机唯一标识
     *
     * @return 返回手机唯一标识，有可能返回空字符串
     */
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return "";
        }
        String deviceId = "";
        try {
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            LogUtils.e("SystemUtil", "获取IMEI失败:" + e);
        }

        if (TextUtils.isEmpty(deviceId)) {
            return "";
        }
        return deviceId;
    }

    /**
     * @return 获取通信地址
     */
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return "";
        }
        WifiInfo info;
        String macAddress = "";
        try {
            info = wifi.getConnectionInfo();
            macAddress = info.getMacAddress();
        } catch (Exception e) {
            LogUtils.e("SystemUtil", "获取Mac失败:" + e);
        }

        if (macAddress == null) {
            return "";
        }
        return macAddress;
    }

    private static final String MARSHMALLOW_MAC_ADDRESS = "02:00:00:00:00:00";
    private static final String FILE_ADDRESS_MAC = "/sys/class/net/wlan0/address";

    public static String getAdresseMAC(Context context) {
        WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        if (wifiInf != null && MARSHMALLOW_MAC_ADDRESS.equals(wifiInf.getMacAddress())) {
            String result = null;
            try {
                result = getAdressMacByInterface();
                if (result != null) {
                    return result;
                } else {
                    result = getAddressMacByFile(wifiMan);
                    return result;
                }
            } catch (IOException e) {
                Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
            } catch (Exception e) {
                Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
            }
        } else {
            if (wifiInf != null && wifiInf.getMacAddress() != null) {
                return wifiInf.getMacAddress();
            } else {
                return "";
            }
        }
        return MARSHMALLOW_MAC_ADDRESS;
    }

    /**
     * 获取手机的ip
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getAdressMacByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if ("wlan0".equalsIgnoreCase(nif.getName())) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
        }
        return null;
    }

    private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
        String ret;
        int wifiState = wifiMan.getWifiState();

        wifiMan.setWifiEnabled(true);
        File fl = new File(FILE_ADDRESS_MAC);
        FileInputStream fin = new FileInputStream(fl);
        ret = crunchifyGetStringFromStream(fin);
        fin.close();

        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return ret;
    }

    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }

    /**
     * 判断网络是否可用
     *
     * @return true 说明网络有效，反之无效
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager == null ? null : mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();

    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(String::compareTo);

        sortMap.putAll(map);

        return sortMap;
    }

    public static void toast(Context context, String message) {
        if (context == null) {
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int byteToKb(int bt) {
        return bt / 1024;
    }


    public static int getTargetSdkVersion(Context context) {
        int targetSdkVersion = -1;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return targetSdkVersion;
    }

    /**
     * 获取App版本名
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本名
     */
    public static String getAppVersionName(Context context, String packageName) {
        String appVersionName = "";
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                appVersionName = pi == null ? null : pi.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appVersionName;
    }

    /**
     * 获取App版本号
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本号
     */
    public static int getAppVersionCode(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 获取应用清单列表钟所有的meta节点
     *
     * @param context 上下文
     * @return 保存着所有meta节点
     */
    public static Bundle getMetaData(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            if (appInfo != null && appInfo.metaData != null) {
                return appInfo.metaData;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return new Bundle();
    }


    /**
     * 获取应用程序名称
     *
     * @param context 上下文
     * @return 应用名称
     */
    public static String getAppName(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageManager pManager = context.getPackageManager();
            ApplicationInfo applicationInfo = pManager.getApplicationInfo(packageName, 0);
            String appName = (String) pManager.getApplicationLabel(applicationInfo);
            if (appName != null && appName.length() != 0) {
                return appName;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序app类名
     *
     * @param context 上下文
     * @return 应用类名
     */
    public static String getApplicationClassName(Context context) {
        try {
            PackageManager pManager = context.getPackageManager();
            ApplicationInfo applicationInfo = pManager.getApplicationInfo(context.getPackageName(), 0);
            String className = applicationInfo.className;
            if (className != null && className.length() != 0) {
                return className;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取密钥散列（FB需要用到）
     * <p>
     * 不同签名文件打包出来的value是不同的
     *
     * @param context
     * @return 签名文件对应散列
     */
    public static String getKeyHash(Context context) {
        if (context == null) {
            return null;
        }

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (NameNotFoundException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return null;
    }

}
