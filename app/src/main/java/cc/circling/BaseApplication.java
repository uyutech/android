package cc.circling;

import android.app.Application;
import android.content.Context;

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
    }

    public static Context getContext() {
        return context;
    }
}
