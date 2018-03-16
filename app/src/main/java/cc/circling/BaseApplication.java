package cc.circling;

import android.app.Application;
import android.content.Context;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

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

        UMConfigure.init(this, "5a27df1fb27b0a06f2000050", "normal", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

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
