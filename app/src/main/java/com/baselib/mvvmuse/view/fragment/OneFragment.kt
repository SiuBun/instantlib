package com.baselib.mvvmuse.view.fragment

import androidx.lifecycle.ViewModelProvider
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvvmuse.viewmodel.OneFragmentViewModel
import com.baselib.use.R
import com.baselib.use.databinding.LayoutFragmentOneBinding


class OneFragment : AbsMvvmFragment<LayoutFragmentOneBinding, OneFragmentViewModel>() {
    override fun initViewModel() = ViewModelProvider(this).get(OneFragmentViewModel::class.java)
    override fun getContentLayout(): Int = R.layout.layout_fragment_one

    override fun lazyLoadData() {
        super.lazyLoadData()
        showEmpty()
    }

    companion object {
        fun getInstance() = OneFragment()
    }
}
