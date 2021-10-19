package com.baselib.instant.util;

import android.util.Log;

import com.baselib.instant.Const;

import androidx.annotation.NonNull;

/**
 * log工具类
 *
 * @author wsb
 */
public class LogUtils {
    private static final String TAG = Const.TAG;
    private static boolean LOG_SWITCH = false;

    public static void setLogSwitch(boolean logSwitch) {
        LOG_SWITCH = logSwitch;
    }

    public static void i(@NonNull Object... args) {
        if (LOG_SWITCH) {
            Log.i(TAG, getStringBuilder(args));
        }
    }

    public static void w(@NonNull Object... args) {
        if (LOG_SWITCH) {
            Log.w(TAG, getStringBuilder(args));
        }
    }

    public static void d(@NonNull Object... args) {
        if (LOG_SWITCH) {
            Log.d(TAG, getStringBuilder(args));
        }
    }

    public static void e(@NonNull Object... args) {
        if (LOG_SWITCH) {
            Log.e(TAG, getStringBuilder(args));
        }
    }

    private static String getStringBuilder(@NonNull Object... args) {
        StringBuilder sb = new StringBuilder();
        if (args.length == 0) {
            sb.append("args is empty");
        } else {
            for (Object object : args) {
                sb.append(object).append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * 生命周期log
     *
     * @param simpleName 类名
     * @param content    方法名
     */
    public static void lifeLog(String simpleName, String content) {
        i(simpleName, content);
    }
}
