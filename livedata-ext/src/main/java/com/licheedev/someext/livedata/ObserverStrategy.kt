package com.licheedev.someext.livedata

import androidx.lifecycle.LiveData

/** 观察者消费事件策略 */
enum class ObserverStrategy {
    /** 多个Observer都能接收1次事件 */
    AfterRegistered,

    /** 【慎用】事件总是能发送到Observer，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样 */
    Always,
}