package com.baselib.mvvmuse.viewmodel

import android.app.Application
import com.baselib.instant.mvvm.viewmodel.BaseViewModel
import com.baselib.mvvmuse.model.OneFragmentModel
import com.baselib.mvvmuse.model.TwoFragmentModel

class TwoFragmentViewModel @JvmOverloads constructor(application: Application, model: TwoFragmentModel? = null) : BaseViewModel<TwoFragmentModel>(application, model) {

}
