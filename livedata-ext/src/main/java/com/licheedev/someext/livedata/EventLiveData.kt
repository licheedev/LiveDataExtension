package com.licheedev.someext.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStore

internal class EventLiveData<T> : LiveData<Event<T>>() {

    @Deprecated("不要调用此函数，请调用observeAlways()、observeSingle()或observePageOnce()")
    override fun observe(owner: LifecycleOwner, observer: Observer<in Event<T>>) {
        //super.observe(owner, observer)
        throw IllegalStateException("不要调用这个方法，请调用observeAlways()、observeSingle()或observePageOnce()")
    }

    @Deprecated(
        "不要调用此函数，此函数无法获得实际的观察者对象，存在内存泄露隐患",
        ReplaceWith("请使用带返回值的函数，safeObserveForever(observer): Observer")
    )
    override fun observeForever(observer: Observer<in Event<T>>) {
        //super.observeForever(observer)
        throw java.lang.IllegalStateException("不要调用此函数，此函数无法获得实际的观察者对象，存在安全隐患，请使用带返回值的observeForever()")
    }

    /**
     * Observer总是能接收到事件，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeAlways(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ): Observer<Event<T>> {
        val wrapper = object : Observer<Event<T>> {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }
                val content = event.content
                observer.onChanged(content)
            }
        }
        super.observe(owner, wrapper)
        return wrapper
    }

    /**
     * 仅1个Observer能接收到事件，且该事件仅能被接收1次
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ): Observer<Event<T>> {
        val wrapper = object : Observer<Event<T>> {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }
                val content = event.contentIfNotHandled
                if (content != null) {
                    observer.onChanged(content)
                }
            }
        }
        super.observe(owner, wrapper)
        return wrapper
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
        observer: Observer<in T>
    ): Observer<Event<T>> {
        val viewModelStoreHash = System.identityHashCode(viewModelStore)
        val wrapper = object : Observer<Event<T>> {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }
                val content = event.getContentIfNotHandled(viewModelStoreHash)
                if (content != null) {
                    observer.onChanged(content)
                }
            }
        }
        super.observe(owner, wrapper)
        return wrapper
    }

    /**
     * 观察者永远都能观察到非超时的事件，直到被移除,跟原版 [LiveData] 的 [LiveData.observeForever] 的行为一样
     * @param observer Observer<in T>
     * @return Observer<Event<T>> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun safeObserveForever(observer: Observer<in T>): Observer<Event<T>> {
        val wrapper = object : Observer<Event<T>> {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }
                observer.onChanged(event.content)
            }
        }
        super.observeForever(wrapper)
        return wrapper
    }


    public override fun setValue(value: Event<T>) {
        value.updateStartTime()
        super.setValue(value)
    }

    public override fun postValue(value: Event<T>?) {
        super.postValue(value)
    }

}