package cc.circling;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by army on 2017/3/16.
 * For my goddess.
 */

public class BaseApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(context, "5a27df1fb27b0a06f2000050", ""));
        MobclickAgent.enableEncrypt(true);

        Bugly.init(getApplicationContext(), "e8be097834", false);
    }

    public static Context getContext() {
        return context;
    }
}
