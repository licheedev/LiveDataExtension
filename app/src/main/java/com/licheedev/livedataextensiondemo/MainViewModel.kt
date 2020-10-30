package com.licheedev.livedataextensiondemo

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.licheedev.someext.livedata.AsyncJob
import com.rxjava.rxlife.life
import rxhttp.RxHttp

class MainViewModel : ViewModel() {
    /** 加载百度首页任务数据 */
    val loadBaiduJob = AsyncJob<String>()

    /** 加载百度首页 */
    fun loadBaidu(lifecycleOwner: LifecycleOwner) {
        
        loadBaiduJob.postBegin()
        
        RxHttp.get("https://www.baidu.com/")
            .asString()
            .life(lifecycleOwner)
            .subscribe({
                loadBaiduJob.postSuccess(it)
            }, {
                loadBaiduJob.postFailure(it)
            })
    }
}