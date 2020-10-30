# LiveDataExtension
基于LiveData的一些扩展类。解决LiveData数据倒灌，提供类似onSuccss、onFailure等多状态异步回调方法。

## 代码实现参考以下项目/文章，感谢
> https://github.com/Flywith24/WrapperLiveData
https://github.com/KunMinX/UnPeek-LiveData
https://juejin.im/post/6844903623252508685


## 用法
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://dl.bintray.com/licheedev/maven' }
  }
}

  dependencies {
        implementation 'com.licheedev:livedata-ext:1.0.0'
}
```

## 代码示例
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
}
```

其他用法可以参考

Kotlin用法： [KtMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/KtMainActivity.kt)

Java用法： [JMainActivity.kt](https://github.com/licheedev/LiveDataExtension/blob/master/app/src/main/java/com/licheedev/livedataextensiondemo/JMainActivity.kt)





