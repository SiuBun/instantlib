package com.baselib.instant.floatwindow;

import android.app.Activity;

/**
 * 悬浮窗游客展示模式
 *
 * @author wsb
 * Created by wsb on 2018/11/21.
 */

public class TouristsFloatMode extends AbstractShowMode {
    @Override
    public void showFloatButton(Activity activity, FloatEventListener listener) {
        FloatButton.show(activity, false, false, false, false,listener);
    }
}
