package cc.circling;

import android.app.Application;
import android.content.Context;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;

import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/16.
 * For my goddess.
 */

public class BaseApplication extends Application {
    private static Context context;
    private static CloudPushService pushService;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        initCloudChannel(this);

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(context, "5a27df1fb27b0a06f2000050", ""));
        MobclickAgent.enableEncrypt(true);

        Bugly.init(context, "e8be097834", !BuildConfig.ENV.equals("prod"));
    }

    public static Context getContext() {
        return context;
    }
    public static CloudPushService getCloudPushService() {
        return pushService;
    }

    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                LogUtil.i("init cloudchannel success");
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                LogUtil.i("init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }
}
