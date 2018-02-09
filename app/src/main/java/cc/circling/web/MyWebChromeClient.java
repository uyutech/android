package cc.circling.web;

import android.net.Uri;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import cc.circling.MainActivity;
import cc.circling.WebFragment;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {
    private WebFragment webFragment;
    private MainActivity mainActivity;

    private ValueCallback<Uri> valueCallback;
    private ValueCallback<Uri[]> filePathCallback;

    public MyWebChromeClient(WebFragment webFragment, MainActivity mainActivity) {
        super();
        this.webFragment = webFragment;
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
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        LogUtil.i("onShowFileChooser");
        this.filePathCallback = filePathCallback;
        mainActivity.album(9);
        return true;
    }
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        LogUtil.i("openFileChooser");
        this.valueCallback = valueCallback;
        mainActivity.album(1);
    }
    public void fileChooserCallback(List<Uri> list) {
        LogUtil.i("fileChooserCallback");
        if(filePathCallback != null) {
            filePathCallback.onReceiveValue(list.toArray(new Uri[list.size()]));
        }
        else if(valueCallback != null) {
            valueCallback.onReceiveValue(list.get(0));
        }
    }
}
