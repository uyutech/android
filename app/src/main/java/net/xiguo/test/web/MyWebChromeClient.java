package net.xiguo.test.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import net.xiguo.test.event.H5EventDispatcher;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends WebChromeClient {
    private static final String PREFIX = "h5container.message: ";
    public MyWebChromeClient() {
        super();
    }
    @Override
    public void onReceivedTitle(WebView var1, String var2) {
        LogUtil.i("onReceivedTitle:" + var2);
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        String msg = cm.message();
        LogUtil.i("onConsoleMessage:" + msg);
        if(msg.startsWith(PREFIX)) {
            JSONObject json = JSON.parseObject(msg.substring(PREFIX.length() - 1));
            H5EventDispatcher.dispatch(json);
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
