package com.licheedev.someext.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStore

internal class EventLiveData<T> : LiveData<Event<T>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Event<T>>) {
        //super.observe(owner, observer)
        //super.observe(owner, observer);
        throw IllegalStateException("不要调用这个方法，请调用observeAlways()、observeSingle()或observePageOnce()")
    }

    /**
     * Observer总是能接收到事件，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     *
     * @param owner
     * @param observer
     */
    fun observeAlways(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ) {
        super.observe(owner, Observer { event ->
            val content = event.content
            observer.onChanged(content)
        })
    }

    /**
     * 仅1个Observer能接收到事件，且该事件仅能被接收1次
     *
     * @param owner
     * @param observer
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ) {
        super.observe(owner, Observer { event ->
            val content = event.contentIfNotHandled
            if (content != null) {
                observer.onChanged(content)
            }
        })
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
        observer: Observer<in T>
    ) {
        val viewModelStoreHash = System.identityHashCode(viewModelStore)
        super.observe(owner, Observer { event ->
            val content = event.getContentIfNotHandled(viewModelStoreHash)
            if (content != null) {
                observer.onChanged(content)
            }
        })
    }

    override fun observeForever(observer: Observer<in Event<T>>) {
        //super.observeForever(observer)
        throw IllegalStateException("不推荐调用这个方法，存在安全隐患")
    }
    
    public override fun setValue(value: Event<T>) {
        super.setValue(value)
    }

    public override fun postValue(value: Event<T>) {
        super.postValue(value)
    }
}