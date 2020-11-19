# LiveDataExtension
基于LiveData的一些扩展类。解决LiveData数据倒灌，提供类似onSuccss、onFailure等多状态异步回调方法。

## 代码实现参考以下项目/文章，感谢
> https://github.com/Flywith24/WrapperLiveData </br>
> https://github.com/KunMinX/UnPeek-LiveData </br>
> https://juejin.im/post/6844903623252508685


## 添加依赖
```gradle
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
    implementation 'com.licheedev:livedata-ext:1.0.2'
}
```

## LiveEvent用法
```kotlin
val liveEvent = LiveEvent<String>()
// 或者可配置事件超时，超时后，观察者无法接收到事件
//val liveEvent = LiveEvent<String>(eventTimeout = 5000L)

liveEvent.observe(this) {
    // 跟原版LiveData相似，观察者始终能接收到事件
}

liveEvent.observeSingle(this) {
    // 事件仅能被1个观察者接收到1次
}

liveEvent.observePageOnce(this, viewModelStore) {
    // 事件能被多个页面的观察者接收到，每个页面仅有1个观察者能接收到事件1次
}
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
viewModel.loadBaiduJob.observe(this, viewModelStore) {

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

Kotlin用法： [KtMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/KtMainActivity.kt)

Java用法： [JMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/JMainActivity.java)





