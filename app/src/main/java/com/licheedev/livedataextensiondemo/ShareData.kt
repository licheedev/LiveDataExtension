package com.licheedev.livedataextensiondemo

import androidx.lifecycle.MutableLiveData
import com.licheedev.someext.livedata.AsyncJob
import com.licheedev.someext.livedata.LiveEvent
import com.licheedev.someext.livedata.ObserverStrategy


object ShareData {
    val sampleJob = AsyncJob<String>(ObserverStrategy.Always)
    val sampleEvent = LiveEvent<String>()
    val sampleLiveData = MutableLiveData<String>()

    init {
        sampleJob.setEventTimeout(10000)
    }

}