package com.baselib.mvvmuse.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.baselib.instant.util.LogUtils
import com.baselib.mvvmuse.model.FragmentEntity
import com.baselib.mvvmuse.view.fragment.TempFragment

class Vp2Adapter constructor(fragmentManager: FragmentManager,lifecycle: Lifecycle):FragmentStateAdapter(fragmentManager, lifecycle) {
    private val mDataSource:ArrayList<FragmentEntity> = arrayListOf()

    private val createId= hashSetOf<Long>()
    override fun getItemCount(): Int = mDataSource.size

    override fun createFragment(position: Int): Fragment {
        val tempFragment = TempFragment.getInstance("${mDataSource[position]} $position")
        LogUtils.d("Vp2Adapter","createFragment ${mDataSource[position]} $position")
        createId.add(mDataSource[position].id)
        return tempFragment
    }

//    fun updateData(data:ArrayList<FragmentEntity>) {
//        val size = mDataSource.size
//        mDataSource.addAll(data)
//        notifyItemRangeInserted(size,data.size)
//    }
//
//
//    fun setData(data:ArrayList<FragmentEntity>) {
//        val size = mDataSource.size
//        if (size>0){
//            mDataSource.clear()
//            notifyItemRangeRemoved(0,size)
//        }
//        mDataSource.addAll(data)
//        notifyItemRangeInserted(0,data.size)
//    }

    fun updateDataNotify(data:ArrayList<FragmentEntity>) {
        mDataSource.addAll(data)
        notifyDataSetChanged()
    }

    fun setDataNotify(data:ArrayList<FragmentEntity>) {
        mDataSource.clear()
        mDataSource.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return mDataSource[position].id
    }

    override fun containsItem(itemId: Long): Boolean {
//        return createId.contains(itemId)
        return mDataSource.any { it.id == itemId }
    }

}