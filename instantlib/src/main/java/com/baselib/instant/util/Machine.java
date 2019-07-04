package com.baselib.instant.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author
 * @date: 2015年1月28日
 */
// CHECKSTYLE:OFF
public class Machine {
    public static int LEPHONE_ICON_SIZE = 72;
    private static boolean sCheckTablet = false;
    private static boolean sIsTablet = false;

    // 硬件加速
    public static int LAYER_TYPE_NONE = 0x00000000;
    public static int LAYER_TYPE_SOFTWARE = 0x00000001;
    public static int LAYER_TYPE_HARDWARE = 0x00000002;
    public static boolean IS_FROYO = Build.VERSION.SDK_INT >= 8;
    public static boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= 11;
    public static boolean IS_HONEYCOMB_MR1 = Build.VERSION.SDK_INT >= 12;
    public static boolean IS_ICS = Build.VERSION.SDK_INT >= 14;
    public static boolean IS_ICS_MR1 = Build.VERSION.SDK_INT >= 15
            && Build.VERSION.RELEASE.equals("4.0.4");// HTC oneX 4.0.4系统
    public static boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= 16;
    public static final boolean IS_JELLY_BEAN_3 = Build.VERSION.SDK_INT >= 18; //4.3
    public static final boolean IS_SDK_ABOVE_KITKAT = Build.VERSION.SDK_INT >= 19; //sdk是否4.4或以上
    public static final boolean IS_SDK_ABOVE_L = Build.VERSION.SDK_INT >= 21; //sdk是否5.0或以上
    public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < 11;// 版本小于3.0
    // 5.1.1 SDK版本号
    public static final int SDK_VERSION_CODE_5_1 = 22;        //5.1和5.1.1都是22
    public static final int SDK_VERSION_CODE_5_1_1 = 22;
    // 6.0
    public static final int SDK_VERSION_CODE_6 = 23;

    // SDK 版本判断
    public static final int SDK_VERSION = Build.VERSION.SDK_INT;
    /**
     * SDK >= 8
     */
    public static final boolean HAS_SDK_FROYO = SDK_VERSION >= 8;
    /**
     * SDK >= 9
     */
    public static final boolean HAS_SDK_GINGERBREAD = SDK_VERSION >= 9;
    /**
     * SDK >= 11
     */
    public static final boolean HAS_SDK_HONEYCOMB = SDK_VERSION >= 11;
    /**
     * SDK >= 12
     */
    public static final boolean HAS_SDK_HONEYCOMB_MR1 = SDK_VERSION >= 12;
    /**
     * SDK >= 13
     */
    public static final boolean HAS_SDK_HONEYCOMB_MR2 = SDK_VERSION >= 13;
    /**
     * SDK >= 14
     */
    public static final boolean HAS_SDK_ICS = SDK_VERSION >= 14;
    /**
     * SDK >= 15
     */
    public static final boolean HAS_SDK_ICS_15 = SDK_VERSION >= 15;
    /**
     * SDK >= 15 && 版本为4.0.4
     */
    public static final boolean HAS_SDK_ICS_MR1 = HAS_SDK_ICS_15
            && Build.VERSION.RELEASE.equals("4.0.4");// HTC oneX 4.0.4系统
    /**
     * SDK >= 16
     */
    public static final boolean HAS_SDK_JELLY_BEAN = SDK_VERSION >= 16;
    /**
     * SDK >= 17
     */
    public static final boolean HAS_SDK_JELLY_BEAN_MR1 = SDK_VERSION >= 17;
    /**
     * SDK >= 18
     */
    public static final boolean HAS_SDK_JELLY_BEAN_MR2 = SDK_VERSION >= 18;
    /**
     * SDK >= 19
     */
    public static final boolean HAS_SDK_KITKAT = SDK_VERSION >= 19;
    /**
     * SDK >= 21 android版本是否为5.0以上
     */
    public static final boolean HAS_SDK_LOLLIPOP = SDK_VERSION >= 21;
    /**
     * SDK >= 22 android版本是否为5.1(5.1.1)以上
     */
    public static final boolean HAS_SDK_5_1_1 = SDK_VERSION >= SDK_VERSION_CODE_5_1_1;
    /**
     * SDK >= 23 android版本是否为6.0以上
     */
    public static final boolean HAS_SDK_6 = SDK_VERSION >= SDK_VERSION_CODE_6;
    /**
     * SDK == 23 android版本是否为6.0
     */
    public static final boolean IS_SDK_6 = SDK_VERSION == SDK_VERSION_CODE_6;

    /**
     * SDK < 11
     */
    public static boolean SDK_UNDER_HONEYCOMB = SDK_VERSION < 11; // 版本小于3.0
    /**
     * SDK < 14
     */
    public static boolean SDK_UNDER_ICS = SDK_VERSION < 14;
    /**
     * SDK < 16
     */
    public static boolean SDK_UNDER_JELLY_BEAN = SDK_VERSION < 16;
    /**
     * SDK < 19
     */
    public static boolean SDK_UNDER_KITKAT = SDK_VERSION < 19;
    /**
     * SDK < 20
     */
    public static boolean SDK_UNDER_KITKAT_WATCH = SDK_VERSION < Build.VERSION_CODES.KITKAT_WATCH;
    /**
     * SDK < 21
     */
    public static boolean SDK_UNDER_LOLIP = SDK_VERSION < 21;

    private static Method sAcceleratedMethod = null;

    private final static String LEPHONEMODEL[] = {"3GW100", "3GW101", "3GC100", "3GC101"};
    private final static String MEIZUBOARD[] = {"m9", "M9", "mx", "MX"};
    private final static String M9BOARD[] = {"m9", "M9"};
    private final static String HW_D2_0082_BOARD[] = {"D2-0082", "d2-0082"};
    private final static String ONE_X_MODEL[] = {"HTC One X", "HTC One S", "HTC Butterfly",
            "HTC One XL", "htc one xl", "HTC Droid Incredible 4G LTE", "HTC 802w"};
    private final static String SHOW_DRAWER_MENU_MODEL[] = {"HTC One_M8",
            "LG-F460K", "LG-D850", "LG-D851", "LG-D855", "LG G3", "VS985 4G", "LG-D724", "G Vista"}; //部分用户，在虚拟键上没有menu海苔条，影响功能，做特殊处理
    private final static String KITKAT_WITHOUT_NAVBAR[] = {"xt1030", "xt1080", "droid ultra", "droid maxx"};

    // 运营商
    public static String CMCC = "cm";               // 中国移动
    public static String CHINA_TELECOM = "ct";     // 中国电信
    public static String CHINA_UNICOM = "cu";      // 中国联通
    private static boolean sSupportGLES20 = false;
    private static boolean sDetectedDevice = false;

    // 用于判断设备是否支持绑定widget
    private static boolean sSupportBindWidget = false;
    // 是否已经进行过绑定widget的判断
    private static boolean sDetectedBindWidget = false;

    public static boolean isLephone() {
        final String model = android.os.Build.MODEL;
        if (model == null) {
            return false;
        }
        final int size = LEPHONEMODEL.length;
        for (int i = 0; i < size; i++) {
            if (model.equals(LEPHONEMODEL[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isM9() {
        return isPhone(M9BOARD);
    }

    public static boolean isMeizu() {
        return isPhone(MEIZUBOARD);
    }

    public static boolean isONE_X() {
        return isModel(ONE_X_MODEL);
    }

    public static boolean isHW_D2_0082() {
        return isPhone(HW_D2_0082_BOARD);
    }

    /**
     * 判断是否三星
     *
     * @return
     */
    public static Boolean isSamsung() {
        if (Build.BRAND.contains("samsung")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否nexus
     *
     * @return
     */
    public static Boolean isNexus() {
        if (Build.MODEL.toLowerCase().contains("nexus")) {
            return true;
        }
        return false;
    }

    private static boolean isPhone(String[] boards) {
        final String board = android.os.Build.BOARD;
        if (board == null) {
            return false;
        }
        final int size = boards.length;
        for (int i = 0; i < size; i++) {
            if (board.equals(boards[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSimilarModel(String[] models) {
        final String board = android.os.Build.MODEL;
        if (board == null) {
            return false;
        }
        final int size = models.length;
        try {
            for (int i = 0; i < size; i++) {
                if (board.contains(models[i])
                        || board.contains(models[i].toLowerCase())
                        || board.contains(models[i].toUpperCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isModel(String[] models) {
        final String board = android.os.Build.MODEL;
        if (board == null) {
            return false;
        }
        final int size = models.length;
        try {
            for (int i = 0; i < size; i++) {
                if (board.equals(models[i])
                        || board.equals(models[i].toLowerCase())
                        || board.equals(models[i].toUpperCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断是否中国大陆用户
     *
     * @param context
     * @return
     */
    public static boolean isCnUser(Context context) {
        boolean result = false;

        if (context != null) {
            // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
            TelephonyManager manager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            // SIM卡状态
            boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
            String simOperator = manager.getSimOperator();

            if (simCardUnable || TextUtils.isEmpty(simOperator)) {
                // 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
                // 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
                String curCountry = Locale.getDefault().getCountry();
                if (curCountry != null && curCountry.contains("CN")) {
                    // 如果获取的国家信息是CN，则返回TRUE
                    result = true;
                } else {
                    // 如果获取不到国家信息，或者国家信息不是CN
                    result = false;
                }
            } else if (simOperator.startsWith("460")) {
                // 如果有SIM卡，并且获取到simOperator信息。
                /**
                 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
                 */
                result = true;
            }
        }

        return result;
    }

    /**
     * 获取运营商
     *
     * @param context
     * @return 中国移动、中国联通、中国电信
     */
    public static String getOperator(Context context) {

        if (null == context) {
            return null;
        }

        // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //SIM卡状态
        boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;

        if (simCardUnable) {
            return null;
        }

        String simOperator = manager.getSimOperator();

        if (TextUtils.isEmpty(simOperator)) {
            return null; // 无运营商
        }

        //如果有SIM卡，并且获取到simOperator信息。
        /**
         * 中国大陆的前5位是(46000)
         * 中国移动：46000、46002
         * 中国联通：46001
         * 中国电信：46003
         */
        if (simOperator.equals("46000") || simOperator.equals("46002")) {
            return CMCC;
        } else if (simOperator.equals("46001")) {
            return CHINA_UNICOM;
        } else if (simOperator.equals("46003")) {
            return CHINA_TELECOM;
        }

        return null;
    }

    /**
     * 是否为中国移动用户
     *
     * @param context
     * @return
     */
    public static boolean isCMCCUser(Context context) {
        boolean ret = false;

        if (null == context) {
            return ret;
        }

        // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //SIM卡状态
        boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;

        if (simCardUnable) {
            return ret;
        }

        String simOperator = manager.getSimOperator();

        if (TextUtils.isEmpty(simOperator)) {
            return ret; // 无运营商
        }

        //如果有SIM卡，并且获取到simOperator信息。
        /**
         * 中国大陆的前5位是(46000)
         * 中国移动：46000、46002
         * 中国联通：46001
         * 中国电信：46003
         */
        if (simOperator.equals("46000") || simOperator.equals("46002")) {
            ret = true;
        }

        return ret;
    }

    /**
     * 是否国外用户或国内的有电子市场的用户，true for yes，or false for no
     *
     * @param context
     * @return
     */
    public static boolean isOverSeaOrExistMarket(Context context) {
        boolean result = false;
        boolean isCnUser = isCnUser(context);
        // 全部的国外用户 + 有电子市场的国内用户
        if (isCnUser) {
            // 是国内用户，则进一步判断是否有电子市场
            result = GoogleMarketUtils.Companion.isMarketExist(context);
        } else {
            // 是国外用户
            result = true;
        }
        return result;
    }

    /**
     * 判断当前运营商是否在指定数组内
     *
     * @param areaArray
     * @return
     */
    public static boolean isLocalAreaCodeMatch(String[] areaArray, Context context) {
        if (null == areaArray || areaArray.length == 0) {
            return false;
        }

        // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
        TelephonyManager manager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String simOperator = manager.getSimOperator();
        if (null == simOperator) {
            return false;
        }

        boolean ret = false;
        for (String areastring : areaArray) {
            if (null == areastring || areastring.length() == 0
                    || areastring.length() > simOperator.length()) {
                continue;
            } else {
                String subString = simOperator.substring(0, areastring.length());
                if (subString.equals(areastring)) {
                    ret = true;
                    break;
                } else {
                    continue;
                }
            }
        }
        return ret;
    }

    // 根据系统版本号判断时候为华为2.2 or 2.2.1, Y 则catch
    public static boolean isHuaweiAndOS2_2_1() {
        boolean resault = false;
        String androidVersion = Build.VERSION.RELEASE;// os版本号
        String brand = Build.BRAND;// 商标
        if (androidVersion == null || brand == null) {
            return resault;
        }
        if (brand.equalsIgnoreCase("Huawei")
                && (androidVersion.equals("2.2") || androidVersion.equals("2.2.2")
                || androidVersion.equals("2.2.1") || androidVersion.equals("2.2.0"))) {
            resault = true;
        }
        return resault;
    }

    // 判断当前设备是否为平板
    private static boolean isPad(Context context) {
//		if (DrawUtils.sDensity >= 1.5 || DrawUtils.sDensity <= 0) {
//			return false;
//		}
//		if (DrawUtils.sWidthPixels < DrawUtils.sHeightPixels) {
//			if (DrawUtils.sWidthPixels > 480 && DrawUtils.sHeightPixels > 800) {
//				return true;
//			}
//		} else {
//			if (DrawUtils.sWidthPixels > 800 && DrawUtils.sHeightPixels > 480) {
//				return true;
//			}
//		}
//		return false;
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isTablet(Context context) {
        if (sCheckTablet) {
            return sIsTablet;
        }
        sCheckTablet = true;
        sIsTablet = isPad(context);
        return sIsTablet;
    }


    /**
     * 设置硬件加速
     *
     * @param view
     * @param mode
     */
    public static void setHardwareAccelerated(View view, int mode) {
        if (sLevelUnder3) {
            return;
        }
        try {
            if (null == sAcceleratedMethod) {
                sAcceleratedMethod = View.class.getMethod("setLayerType", new Class[]{
                        Integer.TYPE, Paint.class});
            }
            sAcceleratedMethod.invoke(view, new Object[]{Integer.valueOf(mode), null});
        } catch (Throwable e) {
            sLevelUnder3 = true;
        }
    }

    public static boolean isIceCreamSandwichOrHigherSdk() {
        return Build.VERSION.SDK_INT >= 14;
    }

    /**
     * 获取Android中的Linux内核版本号
     */
    public static String getLinuxKernel() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == process) {
            return null;
        }

        // get the output line
        InputStream outs = process.getInputStream();
        InputStreamReader isrout = new InputStreamReader(outs);
        BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
        String result = "";
        String line;

        // get the whole standard output string
        try {
            while ((line = brout.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result.equals("")) {
            String Keyword = "version ";
            int index = result.indexOf(Keyword);
            line = result.substring(index + Keyword.length());
            if (null != line) {
                index = line.indexOf(" ");
                return line.substring(0, index);
            }
        }
        return null;
    }

    /**
     * 获得手机内存的可用空间大小
     *
     * @author kingyang
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获得手机内存的总空间大小
     *
     * @author kingyang
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获得手机sdcard的可用空间大小
     *
     * @author kingyang
     */
    public static long getAvailableExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获得手机sdcard的总空间大小
     *
     * @author kingyang
     */
    public static long getTotalExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 是否存在SDCard
     *
     * @return
     * @author chenguanyu
     */
    public static boolean isSDCardExist() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (VerifyError error) {
            LogUtils.e("Machine:isSDCardExist", error);
        }
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前的语言
     *
     * @param context
     * @return
     * @author zhoujun
     */
    public static String getLanguage(Context context) {
        String language = context.getResources().getConfiguration().locale.getLanguage();
        return language;
    }

    /**
     * <br>功能简述:获取Android ID的方法
     * <br>功能详细描述:
     * <br>注意:
     *
     * @return
     */
    public static String getAndroidId(Context context) {
        String androidId = null;
        if (context != null) {
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return androidId;
    }

    /**
     * 获取国家
     *
     * @param context
     * @return
     */
    public static String getCountry(Context context) {
        String ret = null;

        try {
            TelephonyManager telManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                ret = telManager.getSimCountryIso().toLowerCase();
            }
        } catch (Throwable e) {
            //			 e.printStackTrace();
        }
        if (ret == null || ret.equals("")) {
            ret = Locale.getDefault().getCountry().toLowerCase();
        }
        return ret;
    }

    /**
     * 判断是否为韩国用户
     *
     * @return
     */
    public static boolean isKorea(Context context) {
        boolean isKorea = false;

        String country = getCountry(context);
        if (country.equals("kr")) {
            isKorea = true;
        }

        return isKorea;
    }

    /**
     * <br>功能简述: 判断当前用户是否是特定国家用户
     * <br>功能详细描述:
     * <br>注意:
     *
     * @param context
     * @param countryCodes 待检测的国家代号集合
     * @return true 指定国家中包含用户当前所在国家
     * false 指定国家中不包含用户当前所在国家
     */
    public static boolean checkUserCountry(Context context, String... countryCodes) {
        boolean included = false;
        String localCountryCode = getCountry(context);
        for (String countryCode : countryCodes) {
            if (countryCode.equals(localCountryCode)) {
                included = true;
                break;
            }
        }
        return included;
    }

    /**
     * 是否支持OpenGL2.0
     *
     * @param context
     * @return
     */
    public static boolean isSupportGLES20(Context context) {
        if (!sDetectedDevice) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            android.content.pm.ConfigurationInfo info = am.getDeviceConfigurationInfo();
            sSupportGLES20 = info.reqGlEsVersion >= 0x20000;
            sDetectedDevice = true;
        }
        return sSupportGLES20;
    }

    public static boolean canHideNavBar() {
        if (isSimilarModel(KITKAT_WITHOUT_NAVBAR)) {
            return false;
        }
        return true;
    }

    public static boolean isSupportBindWidget(Context context) {
        if (!sDetectedBindWidget) {
            sSupportBindWidget = false;
            if (Build.VERSION.SDK_INT >= 16) {
                try {
                    // 在某些设备上，没有支持"android.appwidget.action.APPWIDGET_BIND"的activity
                    Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
                    PackageManager packageManager = context.getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
                    if (list == null || list.size() <= 0) {
                        sSupportBindWidget = false;
                    } else {
                        // 假如有支持上述action的activity，还需要判断是否已经进行了授权创建widget
                        AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed", int.class,
                                ComponentName.class);
                        sSupportBindWidget = true;
                    }
                } catch (NoSuchMethodException e) { // 虽然是4.1以上系统，但是不支持绑定权限，仍按列表方式添加系统widget
                    e.printStackTrace();
                }
            }
            sDetectedBindWidget = true;
        }
        return sSupportBindWidget;
    }

    /**
     * <br>功能简述:判断api是否大于等于9
     * <br>功能详细描述:
     * <br>注意:
     *
     * @return
     */
    public static boolean isSDKGreaterNine() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 9) {
            result = true;
        }
        return result;
    }

    /**
     * <br>功能简述:获取android版本
     * <br>功能详细描述:
     * <br>注意:
     *
     * @return
     */
    public static int getAndroidSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取设备Gmail帐号
     *
     * @return
     */
    public static Pattern compileEmailAddress() {
        return Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
    }

    public static String getGmail(Context context) {
        // API level 8+
        Pattern emailPattern = compileEmailAddress();
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                return possibleEmail;
            }
        }
        return null;
    }

    public static String[] getGmails(Context context) {
        // API level 8+
        Account[] accounts = AccountManager.get(context).getAccountsByType(
                "com.google");
        String[] accounts_str = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            accounts_str[i] = accounts[i].name;
        }
        return accounts_str;
    }

    public static String getBaseDeviceInfo(Context context) {
//		Context context = ApplicationProxy.getContext();
        StringBuilder baseInfo = new StringBuilder();
        try {
            String product = "Product=" + android.os.Build.PRODUCT;
            String phoneModel = "\nPhoneModel=" + android.os.Build.MODEL;
            String kernel = "\nKernel=" + Machine.getLinuxKernel();
            String rom = "\nROM=" + android.os.Build.DISPLAY;
            String board = "\nBoard=" + android.os.Build.BOARD;
            String device = "\nDevice=" + android.os.Build.DEVICE;
            String appVersion = "\nVersion=";
            String density = "\nDensity=";
            if (context != null) {
                appVersion += SystemUtil.Companion.getAppVersionName(context, context.getPackageName());
                density += String.valueOf(context.getResources().getDisplayMetrics().density);
            }
            String androidVersion = "\nAndroidVersion="
                    + android.os.Build.VERSION.RELEASE;
            String totalMemSize = "\nTotalMemSize="
                    + (getTotalInternalMemorySize() / 1024 / 1024)
                    + "MB";
            String freeMemSize = "\nFreeMemSize="
                    + (getAvailableInternalMemorySize() / 1024 / 1024)
                    + "MB";
            String romAppHeapSize = "\nRom App Heap Size="
                    + Integer
                    .toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
                    + "MB";
            baseInfo.append(product).append(phoneModel).append(kernel).append(rom)
                    .append(board).append(device).append(appVersion).append(density)
                    .append(androidVersion).append(totalMemSize).append(freeMemSize)
                    .append(romAppHeapSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baseInfo.toString();
    }


    /**
     * 是否激活设备
     *
     * @param pkg
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    private static boolean isAdminActive(String pkg, Context context) {
        boolean isActive = false;
        Intent intent = new Intent("android.app.action.DEVICE_ADMIN_ENABLED");
        PackageManager packageManager = context.getPackageManager();
        intent.setPackage(pkg);
        List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent,
                0);
        if (list != null && list.size() != 0) {
            if (Build.VERSION.SDK_INT > 7) {
                DevicePolicyManager devicepolicymanager = (DevicePolicyManager) context
                        .getSystemService(Context.DEVICE_POLICY_SERVICE);
                isActive = devicepolicymanager.isAdminActive(new ComponentName(
                        pkg, list.get(0).activityInfo.name));
            }
        }
        return isActive;
    }

    /**
     * sim卡运营商类型
     */
    public static enum SimOperatorType {
        unknow, // 未知
        mobile, // 中国移动
        unicom, // 中国联通
        telecom, // 中国电信
    }

    /**
     * 获取sim卡运营商类型
     *
     * @param context
     * @return 1 for 移动，2 for 联通，3 for 电信，-1 for 不能识别
     */
    public static SimOperatorType getSimOperatorType(Context context) {
        SimOperatorType simOperatorType = SimOperatorType.unknow;
        // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = manager.getSimOperator();
        if (simOperator != null) {
            if (simOperator.startsWith("46000") || simOperator.startsWith("46002")) {
                // 因为移动网络编号46000下的IMSI已经用完，
                // 所以虚拟了一个46002编号，134/159号段使用了此编号
                // 中国移动
                simOperatorType = SimOperatorType.mobile;
            } else if (simOperator.startsWith("46001")) {
                // 中国联通
                simOperatorType = SimOperatorType.unicom;
            } else if (simOperator.startsWith("46003")) {
                // 中国电信
                simOperatorType = SimOperatorType.telecom;
            }
        }
        return simOperatorType;
    }

    /**
     * 获取当前进程id
     *
     * @return
     */
    public static int getCurrentProcessId() {
        return android.os.Process.myPid();
    }

    /**
     * 获取当前进程名称
     *
     * @return
     */
    public static String getCurrentProcessName() {
        return getProcessNameByPid(getCurrentProcessId());
    }

    /**
     * 根据进程id获取进程名称
     *
     * @param pid
     * @return
     */
    public static String getProcessNameByPid(int pid) {
        BufferedReader br = null;
        String cmdline = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/cmdline")));
            cmdline = br.readLine();
        } catch (Exception e) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {

            }
        }
        return cmdline;
    }

//	/**
//	 * 是否是低性能机器
//	 * @return
//	 */
//	public static boolean isLowPerformanceDevice() {
//		return ConfigurationInfo.getDeviceLevel() == ConfigurationInfo.LOW_DEVICE;
//	}
//
//	/**
//	 * 是否是高性能机器
//	 * @return
//	 */
//	public static boolean isHighPerformanceDevice() {
//		return ConfigurationInfo.getDeviceLevel() == ConfigurationInfo.HIGH_DEVICE;
//	}

    /**
     * 网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkOK(Context context) {
        boolean result = false;
        if (context != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        result = true;
                    }
                }
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 悬浮层是否可用
     *
     * @param context
     * @return
     */
    public static boolean isFloatingWindowAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Object object = context.getSystemService(Context.APP_OPS_SERVICE);
                if (object == null) {
                    return true;
                }
                Class localClass = object.getClass();
                Class[] arrayOfClass = new Class[3];
                arrayOfClass[0] = Integer.TYPE;
                arrayOfClass[1] = Integer.TYPE;
                arrayOfClass[2] = String.class;
                Method method = localClass.getMethod("checkOp", arrayOfClass);
                if (method == null) {
                    return true;
                }
                Object[] arrayOfObject1 = new Object[3];
                arrayOfObject1[0] = Integer.valueOf(24);
                arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
                arrayOfObject1[2] = context.getPackageName();
                int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
                return m != android.app.AppOpsManager.MODE_IGNORED;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
