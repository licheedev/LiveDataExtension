package com.licheedev.livedataextensiondemo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.licheedev.myutils.LogPlus;
import com.licheedev.someext.livedata.AsyncData;
import com.licheedev.someext.livedata.AsyncJob;
import com.licheedev.someext.livedata.AsyncJobException;
import com.licheedev.someext.livedata.AsyncJobObserver;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class JMainActivity extends AppCompatActivity {

    private static final String TAG = "JMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncJob<String> sampleJob = new AsyncJob<>();
        sampleJob.set("type", 999);
        sampleJob.observe(this, new AsyncJobObserver<String>() {
            @Override
            public void onBegin() {
                LogPlus.i(TAG, "开始任务，弹个菊花对话框吧,附件=" + getAttachment());
            }

            @Override
            public void onSuccess(String result) {
                LogPlus.i(TAG,
                    "任务成功，结果=" + result + "，判断参数=" + Objects.equals(sampleJob.get("type"), 999));
            }

            @Override
            public void onFailure(@NotNull AsyncJobException e) {
                LogPlus.i(TAG, "任务失败，异常=" + e + ",cause=" + e.getCause());
            }

            @Override
            public void onProgress(int progress) {
                LogPlus.i(TAG, "进度=" + progress);
            }

            @Override
            public void onCustom(@NotNull String key, @NotNull Object value) {
                LogPlus.i(TAG, "任何自定义事件(无数据时，value=key)，key=" + key + ",事件数据=" + value);
            }
        });

        sampleJob.observe(this, new Observer<AsyncData<String>>() {
            @Override
            public void onChanged(AsyncData<String> data) {
                switch (data.getKey()) {
                    case AsyncData.BEGIN: {
                        LogPlus.i(TAG, "开始任务，弹个菊花对话框吧,附件=" + data.getAttachment());
                    }
                    case AsyncData.SUCCESS: {
                        LogPlus.i(TAG, "任务成功，结果=" + data.getSuccess() + "，判断参数=" + Objects.equals(
                            sampleJob.get("type"), 999));
                    }
                    case AsyncData.FAILURE: {
                        LogPlus.i(TAG,
                            "任务失败，异常=" + data.getFailure() + ",cause=" + data.getFailure()
                                .getCause());
                    }
                    case AsyncData.PROGRESS: {
                        LogPlus.i(TAG, "进度=" + data.getProgress());
                    }
                    case "some_custom_key": {
                        LogPlus.i(TAG, "自定义事件(无数据时，value=key)，事件数据=" + data.getValue());
                    }
                    default: {
                        LogPlus.i(TAG, "任何自定义事件(无数据时，value=key)，key="
                            + data.getKey()
                            + ",事件数据="
                            + data.getValue());
                    }
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                sampleJob.postBegin("这是附件"); // 任务开始
                sampleJob.postProgress(99); // 进度
                sampleJob.postSuccess("这是成功数据");
                sampleJob.postFailure("任务失败了", new RuntimeException("实际异常"));
                sampleJob.postCustom("some_custom_key", null); // 自定义事件
                sampleJob.postCustom("other_custom_key", 234); // 自定义事件
            }
        }.start();
    }
}
