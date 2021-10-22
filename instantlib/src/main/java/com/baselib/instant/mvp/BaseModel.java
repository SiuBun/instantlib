package com.baselib.instant.mvp;

import android.content.Context;

/**
 * M层基类
 * <p>
 * 项目中数据相关类都要继承自该类,在该类及其扩展类中完成相关数据处理
 *
 * @author wsb
 */
public class BaseModel {

    /**
     * 数据层销毁回调
     */
    public void onModelDetach(Context context) {

    }
}
