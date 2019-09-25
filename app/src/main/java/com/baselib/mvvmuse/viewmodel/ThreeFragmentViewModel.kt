package com.baselib.mvvmuse.viewmodel

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.baselib.instant.mvvm.viewmodel.BaseViewModel
import com.baselib.mvvmuse.model.OneFragmentModel
import com.baselib.mvvmuse.model.ThreeFragmentModel

class ThreeFragmentViewModel @JvmOverloads constructor(application: Application, model: ThreeFragmentModel? = null) : BaseViewModel<ThreeFragmentModel>(application, model) {

    companion object {
        fun getFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = modelClass.getConstructor(Application::class.java, ThreeFragmentModel::class.java).newInstance(application, ThreeFragmentModel())
        }
    }

    fun reqData() {
        model?.reqData()
    }

}
