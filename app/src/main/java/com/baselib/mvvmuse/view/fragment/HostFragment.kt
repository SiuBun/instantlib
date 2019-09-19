package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.LayoutFragmentHostBinding
import com.baselib.mvvmuse.viewmodel.HostFragmentViewModel

class HostFragment : AbsMvvmFragment<LayoutFragmentHostBinding, HostFragmentViewModel>() {
    private lateinit var pageList: MutableList<Fragment>
    private lateinit var titleList: MutableList<String>

    override fun initViewModel(): HostFragmentViewModel = ViewModelProviders.of(this).get(HostFragmentViewModel::class.java)

    override fun getContentLayout(): Int = R.layout.layout_fragment_host

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initPageList()
        initTitleList()

        dataBinding.vpPage.offscreenPageLimit = 2
        dataBinding.vpPage.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int): Fragment = pageList[p0]

            override fun getCount(): Int = titleList.size

            override fun getPageTitle(position: Int): CharSequence? = titleList[position]

        }.apply {
            notifyDataSetChanged()
        }

        dataBinding.tabHost.setupWithViewPager(dataBinding.vpPage, false)
        showContentView()

    }

    private fun initTitleList() {
        titleList = mutableListOf<String>().apply {
            add("首页")
            add("资讯")
            add("个人")
        }
    }

    private fun initPageList() {
        pageList = mutableListOf<Fragment>().apply {
            add(OneFragment.getInstance())
            add(TwoFragment.getInstance())
            add(ThreeFragment.getInstance())
        }
    }

}