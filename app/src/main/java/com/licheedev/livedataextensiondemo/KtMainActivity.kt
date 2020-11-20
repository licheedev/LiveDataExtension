package com.licheedev.livedataextensiondemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.observe
import com.licheedev.myutils.LogPlus
import com.licheedev.someext.livedata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.RuntimeException

class KtMainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "KtMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAnotherActivity.setOnClickListener {
            startActivity(Intent(this, AnotherActivity::class.java))
        }
        
        val sampleJob = ShareData.sampleJob
        //val sampleJob = AsyncJob<String>(ObserverStrategy.Single)
        //val sampleJob = AsyncJob<String>(ObserverStrategy.Always)
        sampleJob["type"] = 999
        // dsl方式
        sampleJob.observe(this, viewModelStore) {

            handleBegin {
                LogPlus.i(TAG, "开始任务，弹个菊花对话框吧,附件=$attachment")
            }

            handleSuccess {
                LogPlus.i(TAG, "任务成功，结果=$it，判断参数=${sampleJob["type"] == 999}")
            }

            handleFailure { e: AsyncJobException ->
                LogPlus.i(TAG, "任务失败，异常=$e,cause=${e.cause}")
            }

            handleProgress {
                LogPlus.i(TAG, "进度=$it")
            }

            handleCustom("some_custom_key") {
                LogPlus.i(TAG, "自定义事件(无数据时，value=key)，事件数据=$it")
            }

            handleCustom { key: String, value: Any? ->
                LogPlus.i(TAG, "任何自定义事件(无数据时，value=key)，key=$key,事件数据=$value")
            }
        }

        // when/switch-case方式
        sampleJob.observe(this, viewModelStore) { it ->
            when (it.key) {
                AsyncData.BEGIN -> {
                    LogPlus.i(TAG, "开始任务，弹个菊花对话框吧,附件=${it.attachment}")
                }
                AsyncData.SUCCESS -> {
                    LogPlus.i(TAG, "任务成功，结果=${it.getSuccess()}，判断参数=${sampleJob["type"] == 999}")
                }
                AsyncData.FAILURE -> {
                    LogPlus.i(TAG, "任务失败，异常=${it.getFailure()},cause=${it.getFailure().cause}")
                }
                AsyncData.PROGRESS -> {
                    LogPlus.i(TAG, "进度=${it.getProgress()}")
                }
                "some_custom_key" -> {
                    LogPlus.i(TAG, "自定义事件(无数据时，value=key)，事件数据=${it.value}")
                }
                else -> {
                    LogPlus.i(TAG, "任何自定义事件(无数据时，value=key)，key=${it.key},事件数据=${it.value}")
                }
            }
        }

        // 匿名内部类方式（推荐Java代码使用）
        sampleJob.observe(this, viewModelStore, object : AsyncJobObserver<String>() {

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

        GlobalScope.launch(Dispatchers.IO) {
            sampleJob.postBegin("这是附件") // 任务开始
            sampleJob.postProgress(99) // 进度
            sampleJob.postSuccess("这是成功数据")
            sampleJob.postFailure("任务失败了", RuntimeException("实际异常"))
            sampleJob.postCustom("some_custom_key", null) // 自定义事件
            sampleJob.postCustom("other_custom_key", 234) // 自定义事件
        }
        
        btnObserveMore.setOnClickListener {

            // 匿名内部类方式（推荐Java代码使用）
            sampleJob.observe(this, viewModelStore, object : AsyncJobObserver<String>() {

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
}