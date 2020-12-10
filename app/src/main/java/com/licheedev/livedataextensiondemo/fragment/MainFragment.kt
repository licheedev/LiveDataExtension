package com.licheedev.livedataextensiondemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.licheedev.livedataextensiondemo.R
import com.licheedev.livedataextensiondemo.ShareData
import com.licheedev.myutils.LogPlus
import com.licheedev.someext.livedata.betterLifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*


class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sample = ShareData.sampleEvent
        sample.observeFuture(betterLifecycleOwner){
            LogPlus.e(it)
        }

        sample.observe(betterLifecycleOwner) {
            LogPlus.e(it)
        }

        btnAnotherActivity.setOnClickListener {
            findNavController().navigate(R.id.anotherFragment)
        }

        btnLoadBaidu.setOnClickListener {
            sample.postValue("加载百度,time=${System.currentTimeMillis()}")
        }
    }
}