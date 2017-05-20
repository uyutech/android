package net.xiguo.test.web;

import android.net.Uri;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import net.xiguo.test.BaseApplication;
import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by army on 2017/3/18.
 */

public class MyWebViewClient extends WebViewClient {
    private String h5Bridge = null;
    private X5Activity activity;

    public MyWebViewClient(X5Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        return shouldInterceptRequest(uri.toString());
    }
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return shouldInterceptRequest(url);
    }
    private WebResourceResponse shouldInterceptRequest(String url) {
        // 离线包地址拦截本地资源
        if(url.startsWith(URLs.H5_DOMAIN)) {
            String path = url.substring(URLs.H5_DOMAIN.length());
            // 忽略掉search
            int i = path.indexOf('?');
            if(i > 0) {
                path = path.substring(0, i);
            }
            LogUtil.i("shouldInterceptRequest: " + url + ", " + path);
            if (path.endsWith(".html")
                    || path.endsWith(".htm")
                    || path.endsWith(".css")
                    || path.endsWith(".js")
                    || path.endsWith(".png")
                    || path.endsWith(".gif")
                    || path.endsWith(".jpg")
                    || path.endsWith(".jpeg")) {
                String noSepPath = path.replaceAll("/", "__");
                LogUtil.i("shouldInterceptPath: " + path + ", " + noSepPath);
                WebResourceResponse wrr = null;
                InputStream is = null;
                try {
//                InputStream is = BaseApplication.getContext().getResources().openRawResource(R.raw.test);
//                InputStream is = BaseApplication.getContext().getAssets().open("test.html");
                    is = BaseApplication.getContext().openFileInput(noSepPath);
                    if (noSepPath.endsWith(".html")) {
                        wrr = new WebResourceResponse("text/html", "utf-8", is);
                    } else if (noSepPath.endsWith(".htm")) {
                        wrr = new WebResourceResponse("text/html", "utf-8", is);
                    } else if (noSepPath.endsWith(".css")) {
                        wrr = new WebResourceResponse("text/css", "utf-8", is);
                    } else if (noSepPath.endsWith(".js")) {
                        wrr = new WebResourceResponse("application/javascript", "utf-8", is);
                    } else if (noSepPath.endsWith(".png")) {
                        wrr = new WebResourceResponse("image/png", "utf-8", is);
                    } else if (noSepPath.endsWith(".gif")) {
                        wrr = new WebResourceResponse("image/gif", "utf-8", is);
                    } else if (noSepPath.endsWith(".jpg")) {
                        wrr = new WebResourceResponse("image/jpg", "utf-8", is);
                    } else if (noSepPath.endsWith(".jpeg")) {
                        wrr = new WebResourceResponse("image/jpeg", "utf-8", is);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return wrr;
            }
        }
        return null;
    }

    @Override
    public void onPageFinished(WebView view, String args) {
        LogUtil.i("onPageFinished: " + args);
        view.loadUrl("javascript: " + LoadBridge.getBridgeJs());
    }
}
