package com.baselib.mvpuse.entry;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.io.File;

public class AppInstantItemBean implements Comparable {
    private Drawable mAppIcon;
    private String mAppName;
    private int mAppSize;
    private boolean isSystem=false;
    private String mAppPackageName;
    private boolean isRealApp;

    public AppInstantItemBean(boolean isRealApp) {
        this.isRealApp = isRealApp;
    }

    public static AppInstantItemBean parseByPackageInfo(PackageManager packageManager, PackageInfo p) {
        AppInstantItemBean bean = new AppInstantItemBean(true);
        bean.setmAppIcon(p.applicationInfo.loadIcon(packageManager));
        bean.setAppName(packageManager.getApplicationLabel(p.applicationInfo).toString());
        bean.setAppPackageName(p.applicationInfo.packageName);
        bean.setApkPath(p.applicationInfo.sourceDir);
        File file = new File(p.applicationInfo.sourceDir);
        bean.setAppSize((int) file.length());
        int flags = p.applicationInfo.flags;
        //判断是否是属于系统的apk
        bean.setSystem((flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        return bean;
    }

    public static AppInstantItemBean getFalseBean() {
        return new AppInstantItemBean(false);
    }

    public boolean isRealApp() {
        return isRealApp;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    private String apkPath;

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.mAppPackageName = appPackageName;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public void setmAppIcon(Drawable mAppIcon) {
        this.mAppIcon = mAppIcon;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        this.mAppName = appName;
    }

    public int getAppSize() {
        return mAppSize;
    }

    public void setAppSize(int appSize) {
        this.mAppSize = appSize;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return ((AppInstantItemBean)o).mAppName.compareTo(this.mAppName);
    }
}
