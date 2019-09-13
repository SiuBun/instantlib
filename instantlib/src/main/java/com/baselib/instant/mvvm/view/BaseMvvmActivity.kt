package com.baselib.instant.mvvm.view

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.baselib.instant.mvvm.viewmodel.IViewModel

/**
 * mvvm架构基类
 *
 * V层只关注DB对象和VM对象，业务交由VM对象处理，界面交给DB对象进行操作
 * */
abstract class BaseMvvmActivity<DB : ViewDataBinding, VM : IViewModel> : AppCompatActivity() {
    /**
     * ViewDataBinding
     */
    protected lateinit var dataBinding: DB

    /**
     * viewModel
     */
    protected var viewModel: VM? = null

    protected var mViewModelFactory: ViewModelProvider.Factory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        初始化vm层
        viewModel = initViewModel()

//        尝试恢复原先界面
        savedInstanceState?.also {
            getDataFromStateBundle(savedInstanceState)
        }

//        绑定数据并
        dataBinding = DataBindingUtil.setContentView(this, getActivityLayoutId())
        initObserveAndData(savedInstanceState)

//        vm层关联生命周期
        viewModel?.also {
            lifecycle.addObserver(it as LifecycleObserver)
        }
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

    abstract fun getActivityLayoutId(): Int

    override fun onDestroy() {
//        生命周期关联解除
        viewModel?.let {
            lifecycle.removeObserver(it as LifecycleObserver)
        }

        super.onDestroy()
    }

}
