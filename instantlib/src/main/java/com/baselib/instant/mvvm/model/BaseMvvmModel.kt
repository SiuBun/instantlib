package com.baselib.instant.mvvm.model

import com.baselib.instant.util.LogUtils

abstract class BaseMvvmModel :IModel{
    override fun onModelDestroy() {
        LogUtils.i("$this onModelDestroy")
    }
}