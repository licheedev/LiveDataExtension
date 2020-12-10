package com.licheedev.livedataextensiondemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.licheedev.livedataextensiondemo.R

class FragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_with_fragment)
    }
} 