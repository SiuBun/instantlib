package com.baselib.mvvmuse.view.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.view.View
import com.baselib.instant.mvvm.view.AbsMvvmActivity
import com.baselib.use.R
import com.baselib.mvvmuse.view.fragment.HostFragment
import com.baselib.mvvmuse.viewmodel.MvvmTestViewModel
import com.baselib.use.databinding.ActivityMvvmTestBinding
import java.util.*
import java.util.concurrent.TimeUnit

class MvvmTestActivity : AbsMvvmActivity<ActivityMvvmTestBinding, MvvmTestViewModel>() {

    override fun initObserverAndData() {
        super.initObserverAndData()
        viewModel?.also {
            dataBinding.viewModelInLayout = it

            it.text.observe(this, Observer<String> { s -> dataBinding.tvTest.text = s })
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                showContentView()
            }
        },TimeUnit.SECONDS.toMillis(1))
    }
    override fun initViewModel(): MvvmTestViewModel = ViewModelProviders.of(this, MvvmTestViewModel.getFactory(application)).get(MvvmTestViewModel::class.java)

    override fun getContentLayout(): Int = R.layout.activity_mvvm_test

    fun onJump(view: View) {
        dataBinding.fltContrainer.removeAllViews()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flt_contrainer, HostFragment.getInstance())
                .commit()
    }

}