package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.LayoutFragmentThreeBinding
import com.baselib.mvvmuse.viewmodel.OneFragmentViewModel
import com.baselib.mvvmuse.viewmodel.ThreeFragmentViewModel


class ThreeFragment : AbsMvvmFragment<LayoutFragmentThreeBinding, ThreeFragmentViewModel>() {
    override fun initViewModel() = ViewModelProviders.of(this).get(ThreeFragmentViewModel::class.java)
    override fun getContentLayout(): Int = R.layout.layout_fragment_three

    companion object {
        fun getInstance() = ThreeFragment()
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        showEmpty()
    }
}
