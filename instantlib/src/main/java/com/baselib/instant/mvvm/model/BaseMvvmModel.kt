package com.baselib.instant.mvvm.model

import com.baselib.instant.util.LogUtils

/**
 * mvvp架构M层基类
 *
 * 子类从该类进行扩展对数据进行处理和获取
 * @author wsb
 * */
abstract class BaseMvvmModel :IModel{
    override fun onModelDestroy() {
        LogUtils.i("${this::class.java.simpleName} onModelDestroy")
    }
}