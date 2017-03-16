package net.xiguo.test;

import android.app.Application;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

/**
 * Created by army on 2017/3/16.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("BaseApplication", "onCreate");

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                Log.i("BaseApplication", "onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.i("TestActivity", "onDownloadFinish");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.i("TestActivity", "onInstallFinish");
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.i("BaseApplication", "onDownloadProgress:" + i);
            }
        });

        QbSdk.initX5Environment(getApplicationContext(), cb);
    }
}
