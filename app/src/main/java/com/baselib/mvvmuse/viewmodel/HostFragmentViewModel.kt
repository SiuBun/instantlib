package com.baselib.mvvmuse.viewmodel

import android.app.Application
import com.baselib.instant.mvvm.viewmodel.BaseViewModel
import com.baselib.mvvmuse.model.HostFragmentModel

class HostFragmentViewModel @JvmOverloads constructor(application: Application, model: HostFragmentModel? = null) : BaseViewModel<HostFragmentModel>(application, model) {
}
