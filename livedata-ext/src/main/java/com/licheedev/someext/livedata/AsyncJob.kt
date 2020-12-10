package com.licheedev.someext.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.licheedev.someext.livedata.AsyncData.Companion.BEGIN
import com.licheedev.someext.livedata.AsyncData.Companion.FAILURE
import com.licheedev.someext.livedata.AsyncData.Companion.PROGRESS
import com.licheedev.someext.livedata.AsyncData.Companion.SUCCESS

/** 基于LiveData实现，可以用来发送异步任务事件数据，并使用类似[LiveData.observe]的方式对时间数据进行处理。
 *
 * @param strategy 观察者消费事件策略，可以选择:
 * [ObserverStrategy.AfterRegistered] (默认，Observer仅能接收注册后发生的事件）;
 * [ObserverStrategy.Always] (Observer总是能接收到事件)。
 */
class AsyncJob<T>(
    private val strategy: ObserverStrategy = ObserverStrategy.AfterRegistered
) {

    constructor() : this(ObserverStrategy.AfterRegistered)


    private val mProxy = EventLiveData<AsyncData<T>>()


    /** 事件存活时长，毫秒 */
    private var eventTimeout = 0L

    /**
     * 设置事件超时时间，当事件被发送后，超过一定时间，该事件将无法继续被观察到
     * @param eventTimeout Long 毫秒，事件存活的时长，仅time>0时有效
     */
    fun setEventTimeout(eventTimeout: Long) {
        this.eventTimeout = Math.max(eventTimeout, 0L)
    }

    /** 额外的参数 */
    private val params = mutableMapOf<String, Any?>()

    /** 获取参数 */
    @Synchronized
    operator fun get(paramKey: String): Any? {
        return params[paramKey]
    }

    /** 配置参数 */
    @Synchronized
    operator fun set(paramKey: String, paramValue: Any?) {
        params[paramKey] = paramValue
    }

    /**
     * 观察事件
     * @param owner LifecycleOwner
     * @param observer Observer<AsyncData<T>>
     */
    fun observe(
        owner: LifecycleOwner,
        observer: Observer<AsyncData<T>>
    ) {
        when (strategy) {
            ObserverStrategy.Always -> {
                mProxy.observeNormal(owner, observer)
            }
            ObserverStrategy.AfterRegistered -> {
                mProxy.observeFuture(owner, observer)
            }
        }
    }

    /**
     * 观察事件
     * @param owner LifecycleOwner
     * @param observer Observer<AsyncData<T>>
     */
    fun observe(
        owner: LifecycleOwner,
        observer: AsyncJobObserver<T>
    ) {

        this.observe(owner, object : Observer<AsyncData<T>> {
            override fun onChanged(t: AsyncData<T>?) {
                if (t == null) {
                    return
                }

                observer.attachment = t.attachment

                when (t.key) {
                    BEGIN -> {
                        observer.onBegin()
                    }
                    SUCCESS -> {
                        observer.onSuccess(t.value as T)
                    }
                    FAILURE -> {
                        observer.onFailure(t.value as AsyncJobException)
                    }
                    PROGRESS -> {
                        observer.onProgress(t.value as Int)
                    }
                    else -> {
                        observer.onCustom(t.key, t.value)
                    }
                }
            }
        })
    }

    /**
     * 观察事件
     * @param owner LifecycleOwner
     * @param handler Observer<AsyncData<T>>
     */
    fun observe(
        owner: LifecycleOwner,
        handler: AsyncData<T>.() -> Unit
    ) {
        this.observe(owner, Observer {
            if (it != null) {
                handler.invoke(it)
            }
        })
    }
    
    /** 发送开始事件 */
    @JvmOverloads
    fun postBegin(attachment: Any? = null) {
        val data = AsyncData(this, BEGIN, Unit, attachment)
        doPostData(data)
    }

    /** 发送成功数据 */
    @JvmOverloads
    fun postSuccess(t: T, attachment: Any? = null) {
        val data = AsyncData(this, SUCCESS, t!!, attachment)
        doPostData(data)
    }

    /**
     * 发送失败数据
     * @param tr Throwable 失败异常
     * @param attachment Any? 附件
     */
    @JvmOverloads
    fun postFailure(message: String?, attachment: Any? = null) {
        val data = AsyncData(this, FAILURE, AsyncJobException(message), attachment)
        doPostData(data)
    }

    /**
     * 发送失败数据
     * @param message String 异常信息
     * @param tr Throwable? 异常
     * @param attachment Any? 附件
     */
    @JvmOverloads
    fun postFailure(cause: Throwable?, attachment: Any? = null) {
        val data = AsyncData(this, FAILURE, AsyncJobException(cause), attachment)
        doPostData(data)
    }

    /**
     * 发送失败数据
     * @param message String 异常信息
     * @param tr Throwable? 异常
     * @param attachment Any? 附件
     */
    @JvmOverloads
    fun postFailure(message: String?, cause: Throwable?, attachment: Any? = null) {
        val data = AsyncData(this, FAILURE, AsyncJobException(message, cause), attachment)
        doPostData(data)
    }

    /** 发送进度数据 */
    @JvmOverloads
    fun postProgress(progress: Int, attachment: Any? = null) {
        val data = AsyncData(this, PROGRESS, progress, attachment)
        doPostData(data)
    }

    /**
     * 发送自定义数据
     * @param key String
     * @param value Any? 如果value为null，则自动赋值为key
     * @param attachment Any?
     */
    @JvmOverloads
    fun postCustom(key: String, value: Any?, attachment: Any? = null) {
        val data = AsyncData(this, key, value ?: key, attachment)
        doPostData(data)
    }

    private fun doPostData(data: AsyncData<T>) {
        val value = Event(data, eventTimeout)
        if (isUIThread()) {
            mProxy.setValue(value)
        } else {
            //postValue(data)
            mainHandler.post {
                mProxy.setValue(value)
            }
        }
    }

    companion object {

        private val mainHandler = Handler(Looper.getMainLooper())

        private fun isUIThread(): Boolean {
            return Looper.myLooper() == Looper.getMainLooper()
        }
    }

}

/**  */
abstract class AsyncJobObserver<T> {

    /** 附件数据 */
    var attachment: Any? = null
        internal set

    /** 表示任务成功 */
    abstract fun onSuccess(result: T)

    /** 表示开始任务，一般在这里显示个菊花对话框啥的 */
    open fun onBegin() {
    }

    /** 表示任务失败 */
    open fun onFailure(e: AsyncJobException) {
    }

    /** 表示任务进度，可以用来更新进度条什么的 */
    open fun onProgress(progress: Int) {
    }

    /** 表示任务发出自定义数据，任何自定义事件都会被处理 */
    open fun onCustom(key: String, value: Any) {

    }
}


/** 事件数据封装 */
class AsyncData<T>
/**
 * 异步任务返回的数据
 * @param T
 * @param asyncJob AsyncJob<T>
 * @param key String 数据key
 * @param value Any? 数据
 * @param attachment Any? 附件
 * @constructor
 */
internal constructor(
    val asyncJob: AsyncJob<T>,
    val key: String,
    val value: Any,
    val attachment: Any? = null
) {

    companion object {

        /** 表示开始任务，一般在这里显示个菊花对话框啥的 */
        const val BEGIN = "@#ASYNCJOB_BEGINE"

        /** 表示任务进度，可以用来更新进度条什么的 */
        const val PROGRESS = "@#ASYNCJOB_PROGRESS"

        /** 表示任务成功 */
        const val SUCCESS = "@#ASYNCJOB_SUCCESS"

        /** 表示任务失败 */
        const val FAILURE = "@#ASYNCJOB_FAILURE"
    }


    /** 表示开始任务，一般在这里显示个菊花对话框啥的 */
    inline fun handleBegin(action: () -> Unit) {
        if (key == BEGIN) {
            action.invoke()
        }
    }

    /** 表示任务成功 */
    inline fun handleSuccess(action: (result: T) -> Unit) {
        if (key == SUCCESS) {
            action.invoke(this.value as T)
        }
    }


    /** 表示任务失败 */
    inline fun handleFailure(action: (e: AsyncJobException) -> Unit) {
        if (key == FAILURE) {
            action.invoke(this.value as AsyncJobException)
        }
    }


    /** 表示任务进度，可以用来更新进度条什么的 */
    inline fun handleProgress(action: (progress: Int) -> Unit) {
        if (key == PROGRESS) {
            action.invoke(this.value as Int)
        }
    }

    /** 表示任务发出自定义数据 */
    inline fun handleCustom(key: String, action: (value: Any) -> Unit) {
        if (this.key == key) {
            action.invoke(this.value)
        }
    }

    /** 表示任务发出自定义数据，任何自定义事件都会被处理 */
    inline fun handleCustom(action: (key: String, value: Any?) -> Unit) {
        if (isCustom()) {
            action.invoke(this.key, this.value)
        }
    }

    /** 是否为自定义数据 */
    fun isCustom(): Boolean =
        when (key) {
            BEGIN,
            PROGRESS,
            SUCCESS,
            FAILURE -> {
                false
            }
            else -> {
                true
            }
        }

    /** 表示任务成功 */
    fun getSuccess(): T {
        if (key == SUCCESS) {
            return value as T
        }
        throw IllegalStateException("The AsyncJob Data is NOT SUCCESS, value=${value}")
    }

    /** 表示任务失败 */
    fun getFailure(): AsyncJobException {
        if (key == FAILURE) {
            return value as AsyncJobException
        }
        throw IllegalStateException("The AsyncJob Data is NOT FAILURE, value=${value}")
    }

    /** 表示任务进度，可以用来更新进度条什么的 */
    fun getProgress(): Int {
        if (key == PROGRESS) {
            return value as Int
        }
        throw IllegalStateException("The AsyncJob Data is NOT PROGRESS, value=${value}")
    }
}

/** 异步任务异常 */
class AsyncJobException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}