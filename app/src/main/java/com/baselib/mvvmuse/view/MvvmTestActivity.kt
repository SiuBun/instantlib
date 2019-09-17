package com.baselib.mvvmuse.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.baselib.instant.mvvm.view.BaseMvvmActivity
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.ActivityMvvmTestBinding
import com.baselib.mvvmuse.viewmodel.MvvmTestViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class MvvmTestActivity : BaseMvvmActivity<ActivityMvvmTestBinding, MvvmTestViewModel>() {
    override fun initObserveAndData(savedInstanceState: Bundle?) {
        viewModel?.also {
            dataBinding.viewModelInLayout = it

            it.text.observe(this, Observer<String> { s -> dataBinding.tvTest.text = s })
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                showContentView()
            }
        },TimeUnit.SECONDS.toMillis(2))
    }
    override fun initViewModel(): MvvmTestViewModel {
        return ViewModelProviders.of(this, MvvmTestViewModel.getFactory(application)).get(MvvmTestViewModel::class.java)
    }

    override fun getActivityLayoutId(): Int = R.layout.activity_mvvm_test

}