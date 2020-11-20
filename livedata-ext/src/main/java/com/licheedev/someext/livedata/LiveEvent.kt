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
import androidx.lifecycle.ViewModelStore

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


    @Deprecated("不要调用此函数，请调用observeAlways()、observeSingle()或observePageOnce()")
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        //super.observe(owner, observer)
        throw IllegalStateException("不要调用此函数，请调用observeAlways()、observeSingle()或observePageOnce()")
    }

    @Deprecated(
        "不要调用此函数，此函数无法获得实际的观察者对象，存在内存泄露隐患",
        ReplaceWith("请使用带返回值的函数，safeObserveForever(observer): Observer")
    )
    override fun observeForever(observer: Observer<in T>) {
        //super.observeForever(observer);
        throw java.lang.IllegalStateException("不要调用此函数，此函数无法获得实际的观察者对象，存在安全隐患，请使用带返回值的observeForever()")
    }

    @Deprecated("由于内部使用了代理对象，此函数实际上不会移除任何东西")
    override fun removeObserver(observer: Observer<in T>) {
        //super.removeObserver(observer)
        throw java.lang.IllegalStateException("由于内部使用了代理对象，此函数实际上不会移除任何东西")
    }

    /**
     * Observer总是能接收到事件，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeAlways(owner: LifecycleOwner, observer: Observer<in T>): Observer<Event<T>> {
        return mProxy.observeAlways(owner, observer)
    }

    /**
     * 仅1个Observer能接收到事件，且该事件仅能被接收1次
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<T>
    ): Observer<Event<T>> {
        return mProxy.observeSingle(owner, observer)
    }

    /**
     * 多个页面(ViewModelStore)的1个Observer都能接收1次事件
     * @param owner LifecycleOwner
     * @param viewModelStore ViewModelStore
     * @param observer Observer<T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observePageOnce(
        owner: LifecycleOwner, viewModelStore: ViewModelStore,
        observer: Observer<T>
    ): Observer<Event<T>> {
        return mProxy.observePageOnce(owner, viewModelStore, observer)
    }

    /**
     * 观察者永远都能观察到非超时的事件，直到被移除,跟原版 [LiveData] 的 [LiveData.observeForever] 的行为一样
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun safeObserveForever(observer: Observer<in T>): Observer<Event<T>> {
        return mProxy.safeObserveForever(observer)
    }


    override fun removeObservers(owner: LifecycleOwner) {
        mProxy.removeObservers(owner)
    }

    /**
     * 移除实际上注册的观察者对象（即 [observeAlways]、[observeSingle]、[observePageOnce]、[safeObserveForever] 的返回值）
     * @param observer Observer<in Event<T>>
     */
    fun removeWrapperObserver(observer: Observer<in Event<T>>) {
        mProxy.removeObserver(observer)
    }

    @MainThread
    public override fun setValue(value: T) {
        mProxy.setValue(Event(value, eventTimeout))
    }

    public override fun postValue(value: T) {
        mProxy.postValue(Event(value, eventTimeout))
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

    /** 如果调用线程在主线程，则使用 [LiveData.setValue]，否则使用 [Handler.post] 之后 [LiveData.setValue] */
    fun setPostValue(value: T) {
        if (isUIThread()) {
            setValue(value)
        } else {
            mainHandler.post {
                setValue(value)
            }
        }
    }

    companion object {
        private const val TAG = "SafeLiveEvent"

        private val mainHandler = Handler(Looper.getMainLooper())

        private fun isUIThread(): Boolean {
            return Looper.myLooper() == Looper.getMainLooper()
        }
    }
}