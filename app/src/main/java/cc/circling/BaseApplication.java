package cc.circling;

import android.app.Application;
import android.content.Context;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.Md5FileNameGenerator;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;

import cc.circling.utils.LogUtil;
import cc.circling.utils.MediaUrl2IDCache;

/**
 * Created by army on 2017/3/16.
 * For my goddess.
 */

public class BaseApplication extends Application {
    private static Context context;
    private static CloudPushService pushService;
    private static HttpProxyCacheServer proxy;;

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
    public static HttpProxyCacheServer getProxy() {
        if(proxy == null) {
            proxy = new HttpProxyCacheServer.Builder(context)
                .fileNameGenerator(new MyFileNameGenerator())
                .maxCacheSize(1024 * 1024 * 1024)
                .build();
        }
        return proxy;
    }

    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        pushService = PushServiceFactory.getCloudPushService();
        String deviceId = pushService.getDeviceId();
        LogUtil.i("deviceId", deviceId);
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
    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    static class MyFileNameGenerator extends Md5FileNameGenerator {
        @Override
        public String generate(String url) {
            LogUtil.i("generate", url);
            if(MediaUrl2IDCache.containsKey(url)) {
                LogUtil.i("containsKey", MediaUrl2IDCache.get(url));
                return MediaUrl2IDCache.get(url);
            }
            return super.generate(url);
        }
    }
}
