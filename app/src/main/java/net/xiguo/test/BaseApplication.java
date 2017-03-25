package net.xiguo.test;

import android.app.Application;
import android.content.Context;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

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
        CookieSyncManager.createInstance(this);

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                LogUtil.i("onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                LogUtil.i("onCoreInitFinished");
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                LogUtil.i("onDownloadFinish:" + i);
            }

            @Override
            public void onInstallFinish(int i) {
                LogUtil.i("onInstallFinish:" + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                LogUtil.i("onDownloadProgress:" + i);
            }
        });

        QbSdk.initX5Environment(context, cb);
    }

    public static Context getContext() {
        return context;
    }
}
