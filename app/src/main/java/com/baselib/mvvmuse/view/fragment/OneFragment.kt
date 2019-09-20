package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.LayoutFragmentOneBinding
import com.baselib.mvvmuse.viewmodel.OneFragmentViewModel


class OneFragment : AbsMvvmFragment<LayoutFragmentOneBinding, OneFragmentViewModel>() {
    override fun initViewModel() = ViewModelProviders.of(this).get(OneFragmentViewModel::class.java)
    override fun getContentLayout(): Int = R.layout.layout_fragment_one

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lazyLoadData()
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        showEmpty()
    }

    companion object {
        fun getInstance() = OneFragment()
    }
}
