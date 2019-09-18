package com.baselib.instant.mvvm.view

import android.graphics.drawable.AnimationDrawable
import android.view.View

/**
 * V层构造时候所需要遵循的规范
 *
 * @author wsb
 * */
interface IViewBuilder {
    /**
     * 实例化加载阶段界面内容
     * */
    fun initLoadingView(): View?

    /**
     * 实例化空界面内容
     * */
    fun initEmptyView(): View?

    /**
     * 实例化错误界面内容
     * */
    fun initErrorView(): View?

    /**
     * 界面加载阶段的动画
     * */
    fun initLoadAnimate(): AnimationDrawable?

    /**
     * 界面最大的根布局
     * */
    fun getBaseLayout(): Int

    /**
     * 界面本身的布局
     * */
    fun getContentLayout(): Int

    /**
     * 界面空状态下的布局
     * */
    fun getEmptyLayout(): Int

    /**
     * 界面错误状态下的布局
     * */
    fun getErrorLayout(): Int


}
