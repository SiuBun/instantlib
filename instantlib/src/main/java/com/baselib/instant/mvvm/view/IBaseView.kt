package com.baselib.instant.mvvm.view

/**
 * mvvm架构V层操作规范
 *
 * @author wsb
 * */
interface IBaseView {
    /**
     * 展示加载过程状态
     * */
    fun showLoading()

    /**
     * 展示加载完成状态
     * */
    fun showContentView()

    /**
     * 展示加载错误状态
     * */
    fun showError()
}