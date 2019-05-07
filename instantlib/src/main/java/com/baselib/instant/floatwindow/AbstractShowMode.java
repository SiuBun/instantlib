package com.baselib.instant.floatwindow;

import android.app.Activity;

/**
 * 悬浮窗展示模式抽象类
 *
 * @author wsb
 */
public abstract class AbstractShowMode {
    public static final String OPEN_STATE = "1";

    /**
     * 展示悬浮窗按钮
     *
     * @param activity 上下文
     * @param listener 该模式下按钮事件监听
     */
    public abstract void showFloatButton(Activity activity, FloatEventListener listener);

    public void closeFloatButton(){
        FloatButton.close();
    }
}