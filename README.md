# LiveDataExtension
基于LiveData的一些扩展类。解决LiveData数据倒灌，提供类似onSuccss、onFailure等多状态异步回调方法。

## 代码实现参考以下项目/文章，感谢
> https://github.com/Flywith24/WrapperLiveData </br>
> https://github.com/KunMinX/UnPeek-LiveData </br>
> https://juejin.im/post/6844903623252508685


## 添加依赖
```gradle
// 根build.gradle
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}

// 子module build.gradle
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation 'com.licheedev:livedata-ext:1.1.2'
}
```

## LiveEvent用法
```kotlin
val liveEvent = LiveEvent<String>()
// 或者可配置事件超时，超时后，观察者无法接收到事件
// val liveEvent = LiveEvent<String>(eventTimeout = 5000L)

liveEvent.observeFuture(this) {
    // 使用 [observeFuture] 注册的Observer，在注册时不会接收到之前发生过的事件，仅能接收注册之后发生的事件。
}

liveEvent.observeNormal(this) {
    // 使用 [observeNormal] 注册的Observer总是能接收到非超时事件，即跟原版 [LiveData] 的 [LiveData.observe] 的行为一样
}

liveEvent.observeSingle(this) {
    // 若使用 [observeSingle] 注册多个Observer，当事件发生时，只有其中一个Observer（无法确定是哪一个）能接收1次该事件。
}

val wrappedObserver = liveEvent.safeObserveForever {
    // 跟原版LiveData.observeForever()相似
}

liveEvent.removeObserver(wrappedObserver) // 安全移除Observer
```

## AsyncJob用法
ViewModel层：
```kotlin
class MainViewModel : ViewModel() {
    /** 加载百度首页任务数据 */
    val loadBaiduJob = AsyncJob<String>()

    /** 加载百度首页 */
    fun loadBaidu(lifecycleOwner: LifecycleOwner) {
        
        loadBaiduJob.postBegin()
        
        RxHttp.get("https://www.baidu.com/")
            .asString()
            .life(lifecycleOwner)
            .subscribe({
                loadBaiduJob.postSuccess(it)
            }, {
                loadBaiduJob.postFailure(it)
            })
    }
}
```
View层：
```kotlin
btnLoadBaidu.setOnClickListener {
    viewModel.loadBaidu(this)
}
// 加载结果
viewModel.loadBaiduJob.observe(this) {

    handleBegin {
        "显示个菊花吧".showToast()
    }

    handleSuccess {
        tvContent.setText(it)
    }

    handleFailure {
        it.cause?.let {
            tvContent.text = it.toString()
        }
    }
    
    //handleProgress {
    //    // 可选，进度事件
    //}
    //
    //handleCustom("custom_key") {
    //    // 可选，自定义事件
    //}
    //
    //handleCustom { key, value ->
    //    // 可选，任意自定义事件
    //}
}

```

其他用法可以参考

Kotlin用法： [KtMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/activity/KtMainActivity.kt)

Java用法： [JMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/activity/JMainActivity.java)