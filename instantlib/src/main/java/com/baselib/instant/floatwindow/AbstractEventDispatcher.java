package com.baselib.instant.floatwindow;

import android.app.Activity;

/**
 * 悬浮窗按钮处理器抽象类
 *
 * @author wsb
 * Created by wsb on 2018/11/21.
 */

public abstract class AbstractEventDispatcher {
    /**
     * 按钮功能对外方法状态
     * */
    public static final String OPEN_STATE = "1";
    /**
     * 按钮功能对外关闭状态
     * */
    public static final String CLOSE_STATE = "0";

    /**
     * 处理按钮事件
     *
     * @param activity 所需上下文参数
     * */
    public abstract void dispatchEvent(Activity activity);
}
