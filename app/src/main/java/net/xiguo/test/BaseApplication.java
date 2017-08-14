package net.xiguo.test;

import android.app.Application;
import android.content.Context;

import net.xiguo.test.utils.LogUtil;

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
    }

    public static Context getContext() {
        return context;
    }
}
