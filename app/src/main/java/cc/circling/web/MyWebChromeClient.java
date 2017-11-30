package cc.circling.web;

import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.event.H5EventDispatcher;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {
    private static final String PREFIX = "h5container.message: ";

    private X5Activity activity;

    public MyWebChromeClient(X5Activity activity) {
        super();
        this.activity = activity;
    }
    @Override
    public void onReceivedTitle(WebView view, String args) {
        super.onReceivedTitle(view, args);
        LogUtil.i("onReceivedTitle: " + args);
        if(args != null && args.length() > 0) {
            activity.setDefaultTitle(args);
        }
        view.loadUrl("javascript: " + LoadBridge.getBridgeJs());
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        String msg = cm.message();
        LogUtil.i("onConsoleMessage: " + msg);
        if(msg.startsWith(PREFIX)) {
            JSONObject json = JSON.parseObject(msg.substring(PREFIX.length() - 1));
            H5EventDispatcher.dispatch(this.activity, json);
        }
        return false;
    }
    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        LogUtil.i("onJsAlert");
        return false;
    }
}
