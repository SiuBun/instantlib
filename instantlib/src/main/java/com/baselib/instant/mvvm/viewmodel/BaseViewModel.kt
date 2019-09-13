package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import com.baselib.instant.mvvm.model.IModel
import com.baselib.instant.util.LogUtils

/**
 * mvvm架构vm层基类
 *
 * 只关注M层
 * */
abstract class BaseViewModel<M : IModel>@JvmOverloads constructor(application: Application, model: M? = null) : AndroidViewModel(application),
        DefaultLifecycleObserver, IViewModel {

    protected var model: M? = null

    init {
        model?.let {
            this.model = model
        }
    }

    override fun onStart() {
        LogUtils.lifeLog(this::class.simpleName, "IViewModel-onStart")
    }

    override fun onCreate(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onCreate")
    }

    override fun onResume(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onPause")
    }

    override fun onStart(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onStart")
    }

    override fun onStop(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LogUtils.lifeLog(owner.toString(), "onDestroy")
    }

    override fun onCleared() {
        super.onCleared()
        LogUtils.lifeLog(this.toString(), "onCleared")
    }

}
