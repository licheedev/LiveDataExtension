package com.licheedev.someext.livedata

import androidx.lifecycle.Observer

/**
 * 事件观察着
 * @param T
 * @property observerHash Int
 * @constructor
 */
abstract class EventObserver<T> internal constructor(internal val observerHash: Int) :
    Observer<Event<T>> {

} 