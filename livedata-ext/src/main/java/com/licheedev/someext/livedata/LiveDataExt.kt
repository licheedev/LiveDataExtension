package com.licheedev.someext.livedata

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.fragment.app.DialogFragment

/**
 * 如果是[Fragment]，尽量返回 [Fragment.getViewLifecycleOwner] (部分 [DialogFragment.getViewLifecycleOwner] 会抛出异常)，否则原样返回。
 */
val LifecycleOwner.betterLifecycleOwner: LifecycleOwner
    get() {
        if (this is Fragment) {
            try {
                return this.viewLifecycleOwner
            } catch (e: Exception) {
                Log.w("LiveDataExt", "$this,exception=${e.toString()}")
            }
        }
        return this
    }
