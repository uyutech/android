package net.xiguo.test.web;

import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.event.H5EventDispatcher;
import net.xiguo.test.utils.LogUtil;

import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

/**
 * Created by army on 2017/3/22.
 */

public class MyWebChromeClient extends org.xwalk.core.XWalkUIClient {
    private static final String PREFIX = "h5container.message: ";

    private X5Activity activity;

    public MyWebChromeClient(X5Activity activity, XWalkView webView) {
        super(webView);
        this.activity = activity;
    }
    @Override
    public void onReceivedTitle(XWalkView view, String args) {
        super.onReceivedTitle(view, args);
        LogUtil.i("onReceivedTitle: " + args);
        if(args != null && args.length() > 0) {
            activity.setDefaultTitle(args);
        }
        view.loadUrl("javascript: " + LoadBridge.getBridgeJs());
    }
    @Override
    public boolean onConsoleMessage(XWalkView view,
                                    java.lang.String message,
                                    int lineNumber,
                                    java.lang.String sourceId,
                                    XWalkUIClient.ConsoleMessageType messageType) {
//        String message = cm.message();
        LogUtil.i("onConsoleMessage: " + message);
        if(message.startsWith(PREFIX)) {
            JSONObject json = JSON.parseObject(message.substring(PREFIX.length() - 1));
            H5EventDispatcher.dispatch(this.activity, json);
        }
        return false;
    }
}
