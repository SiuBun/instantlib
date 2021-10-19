package com.baselib.mvvmuse.view.fragment

import androidx.lifecycle.ViewModelProviders
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvvmuse.viewmodel.ThreeFragmentViewModel
import com.baselib.use.R
import com.baselib.use.databinding.LayoutFragmentThreeBinding


class ThreeFragment : AbsMvvmFragment<LayoutFragmentThreeBinding, ThreeFragmentViewModel>() {
    override fun initViewModel() =
        ViewModelProviders.of(this, ThreeFragmentViewModel.getFactory(requireActivity().application))
            .get(ThreeFragmentViewModel::class.java)

    override fun getContentLayout(): Int = R.layout.layout_fragment_three

    override fun isFirstVisible(): Boolean = false

    companion object {
        fun getInstance() = ThreeFragment()
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        showContentView()
    }

    override fun initObserverAndData() {
        super.initObserverAndData()
        dataBinding.tvThree.setOnClickListener {
            viewModel?.reqData()
        }
    }
}
