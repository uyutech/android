package cc.circling.web;

import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.bugly.crashreport.CrashReport;

import cc.circling.MainActivity;
import cc.circling.X5Activity;
import cc.circling.event.H5EventDispatcher;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {

    private MainActivity activity;

    public MyWebChromeClient(MainActivity activity) {
        super();
        this.activity = activity;
    }
    @Override
    public void onReceivedTitle(WebView view, String args) {
        super.onReceivedTitle(view, args);
        LogUtil.i("onReceivedTitle: " + args);
        view.evaluateJavascript(LoadBridge.getBridgeJs(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
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
