package com.baselib.instant.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import java.util.List;

/**
 * 系统配置通知器, 如横竖屏切换，语言切换等
 *
 * @author wsb
 */
public class SysConfigChangedObserver extends BaseObserver<SysConfigChangedObserver.ISysConfigChanged> {
    private int mOrientation;

    private SysConfigChangedObserver(Context context) {
        super(context);
        Configuration config = context.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    @Override
    protected IntentFilter getIntentFilter() {
        return null;
    }

    @Override
    protected BroadcastReceiver getReceiver() {
        return null;
    }


    /**
     * Activity或Service的onConfigurationChanged事件里调用此方法处理事件
     *
     * @param newConfig 新配置内容
     */
    public void onConfigurationChanged(Configuration newConfig) {
        if (null == newConfig) {
            return;
        }

        if (mOrientation != newConfig.orientation) {
            mOrientation = newConfig.orientation;
            notifyOrientationChanged(mOrientation);
        }

    }

    /**
     * 方向状态变化
     *
     * @param newOrientation 新方向值 如{@link ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE}为0
     */
    private void notifyOrientationChanged(int newOrientation) {
        List<ISysConfigChanged> listeners = getListenersCopy();
        for (ISysConfigChanged listener : listeners) {
            if (listener != null) {
                listener.onOrientationChanged(newOrientation);
            }
        }
    }

    /**
     * 系统配置变化监听器
     *
     * @author wsb
     */
    public interface ISysConfigChanged extends ISubscriber {
        /**
         * 屏幕方向变化
         *
         * @param newOrientation
         * @see Configuration#ORIENTATION_PORTRAIT
         * @see Configuration#ORIENTATION_LANDSCAPE
         */
        void onOrientationChanged(int newOrientation);

//		void onLaunguageChanged();
    }
}
