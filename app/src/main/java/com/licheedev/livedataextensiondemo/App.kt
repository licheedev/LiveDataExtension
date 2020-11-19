package com.licheedev.livedataextensiondemo

import android.app.Application
import com.licheedev.someext.livedata.LiveEvent
import okhttp3.OkHttpClient
import rxhttp.RxHttp
import rxhttp.wrapper.ssl.HttpsUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 创建okhttp
        val builder = OkHttpClient.Builder()
        //https全通，不安全
        val sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        // 配置rxhttp
        RxHttp.setDebug(false)
        RxHttp.init(builder.build())



    }
}