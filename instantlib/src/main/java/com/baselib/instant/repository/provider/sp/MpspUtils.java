package com.baselib.instant.repository.provider.sp;

import android.content.Context;
import android.os.DeadObjectException;

public class MpspUtils {
    /**
     * 处理Package manager has died异常
     *
     * @param e      异常
     * @param result 期望返回内容
     */
    public static <T> T processPmhdException(RuntimeException e, T result) {
        if (isPkgManagerDied(e)) {
            return result;
        } else {
            throw e;
        }
    }

    /**
     * 判断异常属不属于DeadSystemException或携带Package manager has died内容，如果是可以进行相关处理
     * <p>
     * android.os.DeadSystemException
     * at android.app.ApplicationPackageManager.getPackageInfoAsUser(ApplicationPackageManager.java:149)
     * at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:137)
     * at com.jb.ga0.commerce.util.io.MultiProcessSharedPreferences.checkInitAuthority(MultiProcessSharedPreferences.java:207)
     * at com.jb.ga0.commerce.util.io.MultiProcessSharedPreferences.getValue(MultiProcessSharedPreferences.java:492)
     * at com.jb.ga0.commerce.util.io.MultiProcessSharedPreferences.getString(MultiProcessSharedPreferences.java:279)
     */
    private static boolean isPkgManagerDied(Exception e) {
        return e instanceof RuntimeException
                && e.getMessage() != null
                && e.getCause() != null
                && (
                (e.getMessage().contains("Package manager has died") && e.getCause() instanceof DeadObjectException)
                        || (e.getMessage().contains("android.os.DeadSystemException"))
        );
    }

    /**
     * 如果设备处在“安全模式”下，只有系统自带的ContentProvider才能被正常解析使用；
     */
    public static boolean isSafeMode(Context context) {
        boolean isSafeMode;
        try {
            isSafeMode = context.getPackageManager().isSafeMode();
        } catch (RuntimeException e) {
            // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.isSafeMode(ApplicationPackageManager.java:820)
            isSafeMode = MpspUtils.processPmhdException(e, false);
        }
        return isSafeMode;
    }



}
