package com.baselib.instant.mvvm.viewmodel

/**
 * mvvm架构VM层操作规范类
 *
 * @author wsb
 * */
interface IViewModel {

    /**
     * 改变当前界面是否已准备好加载数据状态
     *
     * @param prepared true代表可以加载
     * */

    fun changePreparedState(prepared: Boolean)

    /**
     * 获取当前界面准备状态
     *
     * @return true代表可以加载
     * */
    fun getPreparedState(): Boolean

    /**
     * 改变当前界面是否已执行过加载数据
     *
     * @param loadedOnce true代表已加载过
     * */
    fun changeLoadedOnceState(loadedOnce: Boolean)


    /**
     * 获取当前界面加载过与否
     *
     * @return loadedOnce true代表已加载过
     * */
    fun getLoadedOnceState(): Boolean
}