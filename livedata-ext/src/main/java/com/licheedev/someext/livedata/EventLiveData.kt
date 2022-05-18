package com.licheedev.someext.livedata

import android.util.SparseIntArray
import androidx.core.util.forEach
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

internal class EventLiveData<T> : LiveData<Event<T>>() {

    private val handledArray = SparseIntArray()

    @Deprecated("不要调用此函数，请调用observeNormal()、observeSingle()或observeFuture()")
    override fun observe(owner: LifecycleOwner, observer: Observer<in Event<T>>) {
        //super.observe(owner, observer)
        throw IllegalStateException("不要调用此函数，请调用observeNormal()、observeSingle()或observeMulti()")
    }

    @Deprecated(
        "不要调用此函数，此函数无法获得实际的观察者对象，存在内存泄露隐患",
        ReplaceWith("请使用带返回值的函数，safeObserveForever(observer): Observer")
    )
    override fun observeForever(observer: Observer<in Event<T>>) {
        //super.observeForever(observer)
        throw java.lang.IllegalStateException("不要调用此函数，此函数无法获得实际的观察者对象，存在安全隐患，请使用带返回值的safeObserveForever()")
    }

    /**
     * 使用 [observeNormal] 注册的Observer总是能接收到非超时事件，即跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeNormal(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ): EventObserver<T> {
        val wrapper = object : EventObserver<T>(0) {
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
     * 【慎用】若使用 [observeSingle] 注册多个Observer，当事件发生时，只有其中一个Observer（无法确定是哪一个）能接收1次该事件。
     *  该事件被消费后且未超时，仅可以被使用 [observeNormal] 注册的Observer消费。
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeSingle(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ): EventObserver<T> {
        val wrapper = object : EventObserver<T>(0) {
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
     * 使用 [observeFuture] 注册的Observer，在注册时不会接收到之前发生过的事件，仅能接收注册之后发生的事件。
     * @param owner LifecycleOwner
     * @param observer Observer<T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeFuture(
        owner: LifecycleOwner,
        observer: Observer<in T>
    ): EventObserver<T> {
        val hash = System.identityHashCode(observer)
        val wrapper = object : EventObserver<T>(hash) {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }

                if (!shouldHandleEvent(hash)) {
                    return
                }

                val content = event.content
                if (content != null) {
                    observer.onChanged(content)
                }
            }
        }
        //Log.e("注册", hash.toString())
        // 注册的时候，直接标记此观察者已经处理过事件（1）
        // 等setValue的时候，才把状态标记为未处理（0）
        handledArray.put(hash, 1)
        super.observe(owner, wrapper)
        return wrapper
    }

    /**
     * 使用 [observeUnhandled] 注册的Observer，接收到未被注入的[observer]消费过的事件
     * @param owner LifecycleOwner
     * @param hashProvider 用来标记已经处理事件的hash，可以用来填入[ViewModel]
     * @param observer Observer<T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun observeUnhandled(
        owner: LifecycleOwner,
        hashProvider: Any,
        observer: Observer<in T>,
    ): EventObserver<T> {
        val hash = System.identityHashCode(hashProvider)
        val wrapper = object : EventObserver<T>(hash) {
            override fun onChanged(event: Event<T>) {
                if (event.isOutdated) {
                    return
                }

                if (!event.checkMarkHandled(hash)) {
                    return
                }

                val content = event.content
                if (content != null) {
                    observer.onChanged(content)
                }
            }
        }
        super.observe(owner, wrapper)
        return wrapper
    }


    /** 判断是否需要处理事件，0为未处理，1为已处理 */
    private fun shouldHandleEvent(hash: Int): Boolean =
        if (handledArray.indexOfKey(hash) < 0 || handledArray.get(hash) > 0) {
            false
        } else {
            handledArray.put(hash, 1)
            true
        }

    /**
     * Observer永远都能观察到非超时的事件，直到被移除，跟原版 [LiveData] 的 [LiveData.observeForever] 的行为一样
     * @param observer Observer<in T>
     * @return EventObserver<T> 实际注册的观察者对象，在移除观察者 [LiveData.removeObserver] 时传入
     */
    fun safeObserveForever(observer: Observer<in T>): EventObserver<T> {
        val wrapper = object : EventObserver<T>(0) {
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


    override fun removeObserver(observer: Observer<in Event<T>>) {
        super.removeObserver(observer)
        if (observer is EventObserver<*>) {
            //Log.e("移除", observer.observerHash.toString())
            handledArray.remove(observer.observerHash)
        }
    }

    private fun SparseIntArray.remove(key: Int): Boolean {
        val index = indexOfKey(key)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }


    public override fun setValue(value: Event<T>) {
        value.updateStartTime()
        handledArray.forEach { key, _ ->
            handledArray.put(key, 0)
        }
        super.setValue(value)
    }

    public override fun postValue(value: Event<T>) {
        super.postValue(value)
    }
}