package com.licheedev.someext.livedata

import androidx.lifecycle.LiveData

/** 观察者消费事件策略 */
enum class ObserverStrategy {

    /** 仅1个Observer能接收到事件，且该事件仅能被接收1次 */
    Single,

    /** 多个Observer都能接收1次事件 */
    Multi,

    /** 【慎用】事件总是能发送到Observer，跟原版 [LiveData] 的 [LiveData.observe] 的行为一样 */
    Always,
}