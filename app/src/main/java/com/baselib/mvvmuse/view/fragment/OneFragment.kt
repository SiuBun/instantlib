package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.use.databinding.LayoutFragmentOneBinding
import com.baselib.use.R
import com.baselib.mvvmuse.viewmodel.OneFragmentViewModel


class OneFragment : AbsMvvmFragment<LayoutFragmentOneBinding, OneFragmentViewModel>() {
    override fun initViewModel() = ViewModelProviders.of(this).get(OneFragmentViewModel::class.java)
    override fun getContentLayout(): Int = R.layout.layout_fragment_one

    override fun lazyLoadData() {
        super.lazyLoadData()
        showEmpty()
    }

    companion object {
        fun getInstance() = OneFragment()
    }
}
