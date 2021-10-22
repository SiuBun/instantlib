package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import com.baselib.instant.mvvm.model.BaseMvvmModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 * mvvm架构vm层rx基类
 *
 * 只关注M层
 * */
abstract class BaseRxViewModel<M : BaseMvvmModel> @JvmOverloads constructor(
    application: Application,
    model: M? = null
) :
    BaseViewModel<M>(application, model) {


    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
        super.onCleared()
    }

}
