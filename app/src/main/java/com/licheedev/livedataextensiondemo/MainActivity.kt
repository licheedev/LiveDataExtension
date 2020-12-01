package com.licheedev.livedataextensiondemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.licheedev.myutils.LogPlus
import com.licheedev.someext.livedata.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    fun Any.showToast() {
        Toast.makeText(applicationContext, this.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLoadBaidu.setOnClickListener {
            viewModel.loadBaidu(this)
        }
        // 加载结果
        viewModel.loadBaiduJob.observe(this) {

            handleBegin {
                "显示个菊花吧".showToast()
            }

            handleSuccess {
                tvContent.setText(it)
            }

            handleFailure {
                it.cause?.let {
                    tvContent.text = it.toString()
                }
            }
            
            //handleProgress {
            //    // 可选，进度事件
            //}
            //
            //handleCustom("custom_key") {
            //    // 可选，自定义事件
            //}
            //
            //handleCustom { key, value ->
            //    // 可选，任意自定义事件
            //}
        }

    }


}