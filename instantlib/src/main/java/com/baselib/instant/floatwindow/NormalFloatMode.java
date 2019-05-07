package com.baselib.instant.floatwindow;

import android.app.Activity;


/**
 * 悬浮窗常规展示模式
 *
 * @author wsb
 * <p>
 * Created by wsb on 2018/11/21.
 */

public class NormalFloatMode extends AbstractShowMode {
    @Override
    public void showFloatButton(Activity activity, FloatEventListener listener) {
        FloatButton.show(
                activity,
                true,
                true,
                true,
                true,
                listener);
    }
}
