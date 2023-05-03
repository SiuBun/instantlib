package com.baselib.mvvmuse.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.baselib.mvvmuse.model.FragmentEntity
import com.baselib.mvvmuse.view.adapter.Vp2Adapter
import com.baselib.use.databinding.LayoutFragmentMultivp2Binding

class MultiVp2Fragment : Fragment() {
    companion object {
        @JvmStatic
        fun getInstance(): MultiVp2Fragment = MultiVp2Fragment()
    }

    private var multiVp2Binding: LayoutFragmentMultivp2Binding? = null
    private lateinit var mVp2Adapter: Vp2Adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val multiVp2Binding = LayoutFragmentMultivp2Binding.inflate(
            inflater,
            container,
            false
        ).also {
            multiVp2Binding = it
        }
        return multiVp2Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mVp2Adapter = Vp2Adapter(childFragmentManager, this.lifecycle)
        multiVp2Binding?.apply {
            vp2.adapter = mVp2Adapter

            val list = arrayListOf(
                FragmentEntity("a", 1),
                FragmentEntity("b", 2),
                FragmentEntity("c", 3),
            )
            mVp2Adapter.setDataNotify(list)

            btnReset.setOnClickListener {
                val newData = arrayListOf(
                    FragmentEntity("e", 22),
                    FragmentEntity("f", 23),
                    FragmentEntity("g", 24),
                    FragmentEntity("h", 25),
                    FragmentEntity("a", 1),
                    FragmentEntity("b", 2),
                )
                mVp2Adapter.setDataNotify(newData)
            }

            btnChange.setOnClickListener {
                val newData = arrayListOf(
                    FragmentEntity("f", 23),
                    FragmentEntity("g", 24),
                    FragmentEntity("h", 25),
                    FragmentEntity("x", 34),
                    FragmentEntity("y", 35),
                    FragmentEntity("z", 36),
                )
                mVp2Adapter.updateDataNotify(newData)
//                mVp2Adapter.setDataNotify(newData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        multiVp2Binding = null
    }
}