package com.baselib.instant.mvvm.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.support.annotation.CallSuper
import com.baselib.instant.mvvm.model.IModel
import com.baselib.instant.util.LogUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


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


    companion object {
        const val SHOW_LOADING = 0x00000001
        const val HIDE_LOADING: Int = 0x00000001.shl(1)
        const val SHOW_ERROR: Int = 0x00000001.shl(2)
        const val HIDE_ERROR: Int = 0x00000001.shl(3)
        const val SHOW_EMPTY: Int = 0x00000001.shl(4)
        const val HIDE_EMPTY: Int = 0x00000001.shl(5)
        const val SHOW_CONTENT: Int = 0x00000001.shl(6)
        const val HIDE_CONTENT: Int = 0x00000001.shl(7)

        const val LOADING: Int = (SHOW_LOADING or HIDE_ERROR or HIDE_EMPTY or HIDE_CONTENT)
        const val LOADED: Int = (HIDE_LOADING or HIDE_ERROR or HIDE_EMPTY or SHOW_CONTENT)
        const val EMPTY: Int = (HIDE_LOADING or HIDE_ERROR or SHOW_EMPTY or HIDE_CONTENT)
        const val ERROR: Int = (HIDE_LOADING or SHOW_ERROR or HIDE_EMPTY or HIDE_CONTENT)

    }

    fun observerStatus(): Observer<Int> = Observer { status ->
        status?.run {
            loadingState.postValue(statusEnabled(status, SHOW_LOADING))
            loadedState.postValue(statusEnabled(status, SHOW_CONTENT))
            emptyState.postValue(statusEnabled(status, SHOW_EMPTY))
            errorState.postValue(statusEnabled(status, SHOW_ERROR))
        }
    }

    private fun statusEnabled(statuses: Int, status: Int): Boolean = (statuses and status)!= 0
//            .apply {LogUtils.d("$statuses 和 $status 与运算为$this")}


    internal val pageState: MutableLiveData<Int> = MutableLiveData()
    internal val loadingState: MutableLiveData<Boolean> = MutableLiveData()
    internal val errorState: MutableLiveData<Boolean> = MutableLiveData()
    internal val emptyState: MutableLiveData<Boolean> = MutableLiveData()

    internal val loadedState: MutableLiveData<Boolean> = MutableLiveData()

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

    override fun changePreparedState(prepared: Boolean) = preparedState.postValue(prepared)

    override fun getPreparedState(): Boolean = preparedState.value ?: false

    override fun changeLoadedOnceState(loadedOnce: Boolean) = loadedOnceState.postValue(loadedOnce)

    override fun getLoadedOnceState(): Boolean = loadedOnceState.value ?: false

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

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        LogUtils.lifeLog(this::class.java.simpleName, "onCleared")
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.clear()
        }
    }

}
