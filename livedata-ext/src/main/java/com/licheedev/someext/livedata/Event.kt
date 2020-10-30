package com.licheedev.someext.livedata

import android.util.SparseIntArray

internal class Event<T>(val content: T) {
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
}