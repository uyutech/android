package net.xiguo.test.web;


import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {
    public MyWebChromeClient() {
        super();
    }
    @Override
    public void onReceivedTitle(WebView var1, String var2) {
        LogUtil.i("onReceivedTitle:" + var2);
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        LogUtil.i("onConsoleMessage:" + cm.message());
        return false;
    }
    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        LogUtil.i("onJsAlert");
        return false;
    }
}
