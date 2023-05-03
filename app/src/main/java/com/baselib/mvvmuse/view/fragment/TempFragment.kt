package com.baselib.mvvmuse.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.baselib.use.databinding.LayoutFragmentTempBinding

class TempFragment : Fragment() {
    companion object {
        @JvmStatic
        fun getInstance(name: String): TempFragment {
            return TempFragment().also {
                val bundle = Bundle()
                bundle.putString("key_name", name)
                it.arguments = bundle
            }
        }
    }

    private var tempBinding: LayoutFragmentTempBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = LayoutFragmentTempBinding.inflate(
            inflater,
            container,
            false
        ).also {
            tempBinding = it
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tempBinding?.apply {
            tvValue.text = (arguments?.getString("key_name", "")) ?: ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tempBinding = null
    }
}