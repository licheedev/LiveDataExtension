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
class SafeLiveEvent<T> : LiveData<T>() {

    private val mProxy = EventLiveData<T>()
    
    /**
     * 【慎用】Observer总是能接收到事件，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     *
     * @param owner
     * @param observer
     */
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        mProxy.observeAlways(owner, observer)
    }

    /**
     * 仅1个Observer能接收到事件，且该事件仅能被接收1次
     *
     * @param owner
     * @param observer
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<T>
    ) {
        mProxy.observeSingle(owner, observer)
    }

    /**
     * 多个页面(ViewModelStore)的1个Observer都能接收1次事件
     *
     * @param owner
     * @param viewModelStore
     * @param observer
     */
    fun observePageOnce(
        owner: LifecycleOwner, viewModelStore: ViewModelStore,
        observer: Observer<T>
    ) {
        mProxy.observePageOnce(owner, viewModelStore, observer)
    }

    override fun observeForever(observer: Observer<in T>) {
        //super.observeForever(observer);
        throw IllegalStateException("不推荐调用这个方法，存在安全隐患")
    }

    @MainThread
    public override fun setValue(value: T) {
        mProxy.setValue(Event(value))
    }

    override fun postValue(value: T) {
        mProxy.postValue(Event(value))
    }

    override fun getValue(): T? {
        return mProxy.value?.content
    }

    companion object {
        private const val TAG = "SafeLiveEvent"
    }
}