package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.support.annotation.CallSuper
import com.baselib.instant.mvvm.model.IModel
import com.baselib.instant.util.LogUtils

/**
 * mvvm架构vm层基类
 *
 * 只关注M层
 * */
abstract class BaseViewModel<M : IModel> @JvmOverloads constructor(application: Application, model: M? = null) : AndroidViewModel(application),
        DefaultLifecycleObserver, IViewModel {

    protected var model: M? = null

    /**
     * 标志位，标志已经初始化完成
     */
    private val preparedState: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private val loadedOnceState: MutableLiveData<Boolean> = MutableLiveData()


    init {
        model?.let {
            this.model = model
        }
        preparedState.value = false
        loadedOnceState.value = false
    }

    override fun changePreparedState(prepared: Boolean) = preparedState.postValue(prepared)

    override fun getPreparedState():Boolean = preparedState.value?:false

    override fun changeLoadedOnceState(loadedOnce: Boolean) = loadedOnceState.postValue(loadedOnce)

    override fun getLoadedOnceState(): Boolean = loadedOnceState.value?:false

    override fun onViewModelStart() {
        LogUtils.lifeLog(this::class.java.simpleName, "IViewModel-onStart")
    }

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onCreate")
        onViewModelStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onPause")
    }

    override fun onStart(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onStart")
    }

    override fun onStop(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner::class.java.simpleName, "onDestroy")
    }

    override fun onCleared() {
        super.onCleared()
        LogUtils.lifeLog(this::class.java.simpleName, "onCleared")
    }

}
