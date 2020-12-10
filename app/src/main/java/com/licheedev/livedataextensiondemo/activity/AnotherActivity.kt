package com.licheedev.livedataextensiondemo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.licheedev.livedataextensiondemo.R
import com.licheedev.livedataextensiondemo.ShareData
import com.licheedev.myutils.LogPlus
import com.licheedev.someext.livedata.AsyncJobException
import com.licheedev.someext.livedata.AsyncJobObserver
import kotlinx.android.synthetic.main.activity_another.*

class AnotherActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnotherActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_another)

        val sampleJob = ShareData.sampleJob
        sampleJob.observe(this, object : AsyncJobObserver<String>() {

            override fun onBegin() {
                LogPlus.i(TAG, "开始任务，弹个菊花对话框吧,附件=${attachment}")
            }

            override fun onSuccess(result: String) {
                LogPlus.i(TAG, "任务成功，结果=${result}，判断参数=${sampleJob["type"] == 999}")
            }

            override fun onFailure(e: AsyncJobException) {
                LogPlus.i(TAG, "任务失败，异常=${e},cause=${e.cause}")
            }

            override fun onProgress(progress: Int) {
                LogPlus.i(TAG, "进度=${progress}")
            }

            override fun onCustom(key: String, value: Any) {
                LogPlus.i(TAG, "任何自定义事件(无数据时，value=key)，key=${key},事件数据=${value}")
            }
        })

    }
}