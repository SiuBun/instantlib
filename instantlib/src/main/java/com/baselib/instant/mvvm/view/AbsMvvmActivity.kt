package com.baselib.instant.mvvm.view

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import com.baselib.instant.R
import com.baselib.instant.databinding.LayoutBaseActivityBinding
import com.baselib.instant.mvvm.viewmodel.IViewModel
import com.baselib.instant.util.LogUtils
import com.baselib.instant.util.StatusBarUtil

/**
 * mvvm架构V层activity基类
 *
 * V层只关注DB对象和VM对象，业务交由VM对象处理，界面交给DB对象进行操作
 *
 * @author wsb
 * */
abstract class AbsMvvmActivity<DB : ViewDataBinding, VM : IViewModel> : AppCompatActivity(),IViewOperate,IViewBuilder {
    /**
     * 准备添加的界面
     */
    protected lateinit var dataBinding: DB

    /**
     * 根布局
     * */
    private lateinit var rootDataBinding: LayoutBaseActivityBinding

    /**
     * 加载动画
     * */
    private var animationDrawable: AnimationDrawable? = null

    /**
     * viewModel
     */
    protected var viewModel: VM? = null

    /**
     * 错误阶段的view
     * */
    private var errorView: View? = null

    /**
     * 加载阶段的view
     * */
    private var loadingView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentLayout())
        onCreateOperation(savedInstanceState)

    }

    override fun setContentView(layoutResID: Int) {
//        获取根部布局对象
        rootDataBinding = DataBindingUtil.inflate(LayoutInflater.from(this), getBaseLayout(), null, false)

//        获取页面布局对象,并准备将页面布局对象装入容器内部
        dataBinding = DataBindingUtil.inflate<DB>(layoutInflater, layoutResID, null, false).also {
            it.root.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        rootDataBinding.root.findViewById<RelativeLayout>(R.id.rlt_container)?.also {
            it.addView(dataBinding.root)
        }
//        填充根部布局对象
        window.setContentView(rootDataBinding.root)

        if (defineStatusBar()) {
            // 设置透明状态栏，兼容4.4
            StatusBarUtil.setColor(this, resources.getColor(R.color.colorTheme), 0)
        }

        loadingView = initLoadingView()

        animationDrawable = initLoadAnimate()

        errorView = initErrorView()?.apply {
            isClickable = true
            setOnClickListener {
                showLoading()
                onRefresh()
            }
        }


//        关联toolbar控件
        if(useToolBar()){
            setToolBar()
        }else{
            rootDataBinding.toolBar.visibility = View.GONE
        }

    }

    /**
     * 初始化加载阶段的动画
     * */
    override fun initLoadAnimate(): AnimationDrawable? = loadingView?.let {
        findViewById<ImageView>(R.id.img_progress).drawable as AnimationDrawable
    }

    /**
     * 是否需要使用toolbar
     * */
    open fun useToolBar(): Boolean = false

    /**
     * 失败后点击刷新
     */
    override fun onRefresh() {
        LogUtils.d("点击进行页面重新刷新")
    }

    override fun showLoading() = runOnUiThread {
        dataBinding.root.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        errorView?.apply {
            visibility = View.GONE
        }

        loadingView?.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
        animationDrawable?.apply {
            if (!isRunning) {
                start()
            }
        }


    }

    override fun showContentView() = runOnUiThread {
        loadingView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        errorView?.apply {
            visibility = View.GONE
        }

        dataBinding.root.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
        animationDrawable?.apply {
            if (isRunning) {
                stop()
            }
        }

    }

    override fun showError() = runOnUiThread {
        loadingView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        dataBinding.root.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        animationDrawable?.apply {
            if (isRunning) {
                stop()
            }
        }

        errorView?.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
    }

    override fun showEmpty() {
    }


    override fun initLoadingView(): View? =
            findViewById<ViewStub>(R.id.vs_loading).takeIf { it != null }?.let { vs ->
                getLoadingLayout().takeIf { it > 0 }?.run {
                    vs.layoutResource = this
                }
                vs.inflate()
            }

    /**
     * 获取指定的加载阶段布局
     *
     * 子类可以重写定义该界面的加载阶段展示
     * */
    open fun getLoadingLayout(): Int = R.layout.layout_loading_view

    override fun initErrorView(): View? =
            findViewById<ViewStub>(R.id.vs_error_refresh).takeIf { it != null }?.let { vs ->
                getErrorLayout().takeIf { it > 0 }?.run {
                    vs.layoutResource = this
                }
                vs.inflate()
            }


    /**
     * 获取指定的加载错误布局
     *
     * 子类可以重写定义该界面的加载错误展示
     * */
    override fun getErrorLayout(): Int = R.layout.layout_loading_error

    override fun initEmptyView(): View? = null

    override fun getEmptyLayout(): Int = 0

    /**
     * 设置titlebar
     */
    private fun setToolBar() {
        setSupportActionBar(rootDataBinding.toolBar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.icon_back)
        }

        rootDataBinding.toolBar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                onBackPressed()
            }
        }
    }

    override fun setTitle(text: CharSequence) {
        this.rootDataBinding.toolBar.title = text
    }

    /**
     * 是否定义状态栏
     *
     * @return true说明沉浸式状态栏
     * */
    open fun defineStatusBar(): Boolean = false

    /**
     * 用于获取通用布局
     * */
    override fun getBaseLayout(): Int = R.layout.layout_base_activity

    private fun onCreateOperation(savedInstanceState: Bundle?) {
        //        初始化vm层
        viewModel = initViewModel().also {
            lifecycle.addObserver(it as LifecycleObserver)
        }

        //        尝试恢复原先界面
        savedInstanceState?.also {
            getDataFromStateBundle(savedInstanceState)
        }

        showLoading()
        initObserveAndData(savedInstanceState)

    }

    /**
     * 数据及观察对象初始化
     *
     * @param savedInstanceState Bundle
     */
    abstract fun initObserveAndData(savedInstanceState: Bundle?)

    /**
     * 从状态保存的数据中获取以便恢复数据
     *
     * 和[setDataIntoStateBundle]相对应
     *
     * @param savedInstanceState 保存数据的对象
     * */
    open fun getDataFromStateBundle(savedInstanceState: Bundle) {}

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        setDataIntoStateBundle(outState)
    }

    /**
     * 将数据保存到数据对象中以便后面恢复
     *
     * 和[getDataFromStateBundle]方法对应
     *
     * @param outState 存放数据的对象
     * */
    open fun setDataIntoStateBundle(outState: Bundle?) {}

    abstract fun initViewModel(): VM

    fun <T : ViewModel> initViewModelByClz(clazz: Class<T>): ViewModel = ViewModelProviders.of(this).get(clazz)

    protected fun <T : View> getView(id: Int): T = findViewById<View>(id) as T

    override fun onDestroy() {
        animationDrawable?.run{
            if(isRunning){
                stop()
            }
        }
//        生命周期关联解除
        viewModel?.run {
            lifecycle.removeObserver(this as LifecycleObserver)
        }

        super.onDestroy()
    }

}
