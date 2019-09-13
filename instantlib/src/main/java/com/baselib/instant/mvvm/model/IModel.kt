package com.baselib.instant.mvvm.model

/**
 * mvvm架构M层行为规范类
 *
 * @author wsb
 * */
interface IModel {
    /**
     * 进行资源回收释放
     */
    fun onModelDestroy()
}
