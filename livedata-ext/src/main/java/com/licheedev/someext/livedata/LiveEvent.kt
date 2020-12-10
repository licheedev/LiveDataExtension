/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except compliance with the License.
 *  You may obtaa copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.licheedev.someext.livedata

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * 具有防数据倒灌功能的LiveData
 *
 * @param <T>
 */
class LiveEvent<T>() : LiveData<T>() {


    /** 事件存活时长，毫秒 */
    private var eventTimeout = 0L

    /**
     * 具有防数据倒灌功能的LiveData
     * @param eventTimeout Long 毫秒，事件存活的时长，仅time>0时有效。当事件被发送后，超过一定时间，该事件将无法继续被观察到
     * @constructor
     */
    constructor(eventTimeout: Long) : this() {
        setEventTimeout(eventTimeout)
    }

    /**
     * 设置事件超时时间，当事件被发送后，超过一定时间，该事件将无法继续被观察到
     * @param eventTimeout Long 毫秒，事件存活的时长，仅time>0时有效
     */
    fun setEventTimeout(eventTimeout: Long) {
        this.eventTimeout = Math.max(eventTimeout, 0L)
    }

    private val mProxy = EventLiveData<T>()

    @Deprecated(
        "不要调用此函数，此函数无法获得实际的观察者对象，存在内存泄露隐患",
        ReplaceWith("请使用带返回值的函数，safeObserveForever(observer): EventObserver")
    )
    override fun observeForever(observer: Observer<in T>) {
        //super.observeForever(observer);
        throw java.lang.IllegalStateException("不要调用此函数，此函数无法获得实际的观察者对象，存在安全隐患，请使用带返回值的safeObserveForever()")
    }

    @Deprecated("由于内部使用了代理对象，此函数实际上不会移除任何东西")
    override fun removeObserver(observer: Observer<in T>) {
        //super.removeObserver(observer)
        throw java.lang.IllegalStateException("由于内部使用了代理对象，此函数实际上不会移除任何东西")
    }

    @Deprecated("此方法内部使用了observeFuture()，由于不会返回真实的Observer对象，可能存在安全隐患，请尽量使用observeFuture()")
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        mProxy.observeFuture(owner, observer)
    }

    /**
     * 使用 [observeNormal] 注册的Observer总是能接收到非超时事件，即跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeNormal(owner: LifecycleOwner, observer: Observer<in T>): EventObserver<T> {
        return mProxy.observeNormal(owner, observer)
    }

    /**
     * 【慎用】若使用 [observeSingle] 注册多个Observer，当事件发生时，只有其中一个Observer（无法确定是哪一个）能接收1次该事件。
     *  该事件被消费后且未超时，仅可以被使用 [observeNormal] 注册的Observer消费。
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<T>
    ): EventObserver<T> {
        return mProxy.observeSingle(owner, observer)
    }

    /**
     * 使用 [observeFuture] 注册的Observer，在注册时不会接收到之前发生过的事件，仅能接收注册之后发生的事件。
     * @param owner LifecycleOwner
     * @param observer Observer<T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeFuture(
        owner: LifecycleOwner,
        observer: Observer<T>
    ): EventObserver<T> {
        return mProxy.observeFuture(owner, observer)
    }

    /**
     * Observer永远都能观察到非超时的事件，直到被移除，跟原版 [LiveData] 的 [LiveData.observeForever] 的行为一样
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun safeObserveForever(observer: Observer<in T>): EventObserver<T> {
        return mProxy.safeObserveForever(observer)
    }


    override fun removeObservers(owner: LifecycleOwner) {
        mProxy.removeObservers(owner)
    }

    /**
     * 移除实际上注册的观察者对象（即 [observeNormal]、[observeSingle]、[observeFuture]、[safeObserveForever] 的返回值）
     * @param observer Observer<in Event<T>>
     */
    fun removeObserver(observer: EventObserver<T>) {
        mProxy.removeObserver(observer)
    }


    @MainThread
    public override fun setValue(value: T?) {
        mProxy.setValue(Event(value, eventTimeout))
    }

    override fun postValue(value: T) {
        postValue(value, true)
    }

    /**
     * 发送数据，如果在UI线程使用直接[setValue]。
     * 如果在子线程，则根据[useHandlerPost]的值，如果为true，则使用 [Handler.post]来[setValue]；如果为false，则使用[LiveData.postValue]。
     * @param value T?
     * @param useHandlerPost Boolean 如果为true，则使用 [Handler.post]来[setValue]；如果为false，则使用[LiveData.postValue]
     */
    fun postValue(value: T?, useHandlerPost: Boolean = true) {
        if (isUIThread()) {
            setValue(value)
        } else if (useHandlerPost) {
            mainHandler.post {
                setValue(value)
            }
        } else {
            mProxy.postValue(Event(value, eventTimeout))
        }
    }

    override fun getValue(): T? {
        return mProxy.value?.content
    }

    override fun hasObservers(): Boolean {
        return mProxy.hasObservers()
    }

    override fun hasActiveObservers(): Boolean {
        return mProxy.hasActiveObservers()
    }

    companion object {
        private const val TAG = "LiveEvent"

        private val mainHandler = Handler(Looper.getMainLooper())

        private fun isUIThread(): Boolean {
            return Looper.myLooper() == Looper.getMainLooper()
        }
    }
}