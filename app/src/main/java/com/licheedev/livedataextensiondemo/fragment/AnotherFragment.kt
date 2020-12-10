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
import kotlinx.android.synthetic.main.activity_another.*
import kotlinx.android.synthetic.main.activity_another.btnLoadBaidu


class AnotherFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_another, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val sampleEvent = ShareData.sampleEvent
        sampleEvent.observeNormal(this) {
            LogPlus.e(it)
        }

        btnLoadBaidu.setOnClickListener {
            sampleEvent.postValue("加载百度,time=${System.currentTimeMillis()}")
        }
    }
}