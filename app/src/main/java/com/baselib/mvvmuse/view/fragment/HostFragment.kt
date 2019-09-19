package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.LayoutFragmentHostBinding
import com.baselib.mvvmuse.viewmodel.HostFragmentViewModel

class HostFragment : AbsMvvmFragment<LayoutFragmentHostBinding, HostFragmentViewModel>() {

    override fun initViewModel(): HostFragmentViewModel = ViewModelProviders.of(this).get(HostFragmentViewModel::class.java)

    override fun getContentLayout(): Int = R.layout.layout_fragment_host

}