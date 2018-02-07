package cc.circling.web;

import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.tencent.bugly.crashreport.CrashReport;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {

    private MainActivity mainActivity;

    public MyWebChromeClient(MainActivity mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }
    @Override
    public void onReceivedTitle(WebView view, String args) {
        super.onReceivedTitle(view, args);
        LogUtil.i("onReceivedTitle: " + args);
        view.evaluateJavascript(LoadBridge.getBridgeJs(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        LogUtil.i("onShowCustomView", view.getClass().getName());
        super.onShowCustomView(view, callback);
//        activity.fullScreen(view);
    }
    @Override
    public void onHideCustomView() {
        LogUtil.i("onHideCustomView");
        super.onHideCustomView();
//        activity.unFullScreen();
    }
    @Override
    public void onProgressChanged(WebView webView, int progress) {
        // 增加Javascript异常监控
        CrashReport.setJavascriptMonitor(webView, true);
        super.onProgressChanged(webView, progress);
    }
}
