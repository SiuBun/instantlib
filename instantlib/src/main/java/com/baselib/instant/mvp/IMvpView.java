package com.baselib.instant.mvp;

import android.app.Activity;

/**
 * View层基类
 * <p>
 * 项目中视图层控件操作相关类都要继承自该类，子类在继承该类进行view层操作行为扩展
 *
 * @author wsb
 */
public interface IMvpView {
    Activity reqActivity();

    void toast(String content);
}
