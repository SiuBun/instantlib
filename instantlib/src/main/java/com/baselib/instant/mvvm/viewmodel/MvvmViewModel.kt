package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.baselib.instant.mvvm.model.IModel
import com.baselib.instant.util.LogUtils

/**
 * Mvvm架构VM层
 *
 * @author wangshaobin
 * */
abstract class MvvmViewModel<M : IModel> @JvmOverloads constructor(application: Application, model: M? = null) :
    AndroidViewModel(application), DefaultLifecycleObserver {

    var model: M? = null

    init {
        model?.let {
            this.model = it
        }
    }

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onCreate")
    }

    override fun onResume(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onResume")
    }

    private fun getOwnerName(owner: LifecycleOwner) = "${owner::class.java.simpleName}:${this::class.java.simpleName}"

    override fun onPause(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onPause")
    }

    override fun onStart(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onStart")
    }

    override fun onStop(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LogUtils.lifeLog(getOwnerName(owner), "onDestroy")
    }

    override fun onCleared() {
        super.onCleared()
        LogUtils.lifeLog(this::class.java.simpleName, "onCleared")
    }
}