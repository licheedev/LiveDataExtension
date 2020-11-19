package com.licheedev.someext.livedata

import android.os.SystemClock
import android.util.SparseIntArray

internal class Event<T>(val content: T, private val eventSurvivalTime: Long = 0L) {

    var isHasBeenHandled = false
        private set
    private val handledArray = SparseIntArray()
    val contentIfNotHandled: T?
        get() = if (isHasBeenHandled) {
            null
        } else {
            isHasBeenHandled = true
            content
        }

    fun getContentIfNotHandled(viewModelStoreHash: Int): T? {
        return if (handledArray.indexOfKey(viewModelStoreHash) >= 0) {
            null
        } else {
            handledArray.put(viewModelStoreHash, 1)
            content
        }
    }

    /** 事件开始时间 */
    private var startTime = 0L
    
    fun updateStartTime() {
        startTime = SystemClock.uptimeMillis()
    }

    private var outdated: Boolean = false

    /** 事件是否已经过时 */
    val isOutdated: Boolean
        get() {
            if (outdated) {
                return true
            }
            if (eventSurvivalTime > 0 && (SystemClock.uptimeMillis() - startTime) > eventSurvivalTime) {
                outdated = true
            }
            return outdated
        }
}