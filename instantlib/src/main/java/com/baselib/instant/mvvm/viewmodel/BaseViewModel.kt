package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.CallSuper
import com.baselib.instant.mvvm.model.IModel
import com.baselib.instant.util.LogUtils
import io.reactivex.disposables.CompositeDisposable


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

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    internal val startLoading = 0x0001F
    internal  val stopLoading: Int = 0x0002F
    internal  val showError: Int = 0x0003F
    internal  val hideError: Int = 0x0004F
    internal  val showEmpty: Int = 0x0005F
    internal  val hideEmpty: Int = 0x0006F
    internal  val showContent: Int = 0x0007F
    internal  val hideContent: Int = 0x0008F

    private val LOADING: Int = (startLoading or hideError or hideEmpty or hideContent)
    private val LOADED: Int = (stopLoading or hideError or hideEmpty or showContent)
    private val EMPTY: Int = (stopLoading or hideError or showEmpty or hideContent)
    private val ERROR: Int = (stopLoading or showError or hideEmpty or hideContent)

    internal val pageState :MutableLiveData<Int> = MutableLiveData()

    internal val loadingState :MutableLiveData<Boolean> = MutableLiveData()
    internal val errorState :MutableLiveData<Boolean> = MutableLiveData()
    internal val emptyState :MutableLiveData<Boolean> = MutableLiveData()
    internal val loadedState :MutableLiveData<Boolean> = MutableLiveData()

    init {
        model?.let {
            this.model = model
        }
        preparedState.value = false
        loadedOnceState.value = false

        loadedState.value = false
        errorState.value = false
        emptyState.value = false
        loadedState.value = false
    }

    fun isStatusEnabled(statuses: Int, status: Int): Boolean = statuses and status != 0

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

    protected fun addDisposable(disposable: CompositeDisposable){
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        LogUtils.lifeLog(this::class.java.simpleName, "onCleared")
        if (!compositeDisposable.isDisposed){
            compositeDisposable.clear()
        }
    }

}
