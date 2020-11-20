package com.licheedev.someext.livedata

import android.os.SystemClock
import android.util.SparseIntArray

class Event<T>(val content: T, private val eventSurvivalTime: Long = 0L) {

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

    /** 事件是否已经过时 */
    var isOutdated: Boolean = false
        private set
        get() {
            if (field) {
                return true
            }
            if (eventSurvivalTime > 0 && (SystemClock.uptimeMillis() - startTime) > eventSurvivalTime) {
                field = true
            }
            return field
        }
}