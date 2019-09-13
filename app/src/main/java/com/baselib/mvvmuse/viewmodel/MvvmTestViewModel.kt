package com.baselib.mvvmuse.viewmodel

import android.app.Application
import android.arch.core.util.Function
import android.arch.lifecycle.*
import android.databinding.ObservableField
import com.baselib.instant.mvvm.viewmodel.BaseViewModel
import com.baselib.mvvmuse.model.MvvmTestModel

class MvvmTestViewModel @JvmOverloads constructor(application: Application, model: MvvmTestModel?=null) : BaseViewModel<MvvmTestModel>(application, model) {
    val text : MutableLiveData<String> = MutableLiveData()

    init {
        text.value  = "hello"
    }

    fun onTvClick(){
        text.postValue(model?.getTvStr()?:"default")
    }

    companion object{
        fun getFactory(application:Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T = modelClass.getConstructor(Application::class.java, MvvmTestModel::class.java).newInstance(application, MvvmTestModel())
            }
        }
    }

}