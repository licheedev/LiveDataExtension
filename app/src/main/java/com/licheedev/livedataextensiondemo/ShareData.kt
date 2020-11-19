package com.licheedev.livedataextensiondemo

import com.licheedev.someext.livedata.AsyncJob
import com.licheedev.someext.livedata.ObserverStrategy


object ShareData {
    val sampleJob = AsyncJob<String>(ObserverStrategy.PageOnce)

    init {
        sampleJob.setEventTimeout(10000)
    }

}