package com.baselib.instant.mvvm.view

/**
 * mvvm架构V层操作规范
 *
 * @author wsb
 * */
interface IViewOperate {
    /**
     * 展示加载过程状态
     * */
    fun showLoading() :Unit?

    /**
     * 展示加载完成状态
     * */
    fun showContentView():Unit?

    /**
     * 展示加载错误状态
     * */
    fun showError():Unit?

    /**
     * 展示加载空状态
     * */
    fun showEmpty():Unit?

    /**
     * 刷新界面
     * */
    fun onRefresh()

    fun initObserverAndData()
}