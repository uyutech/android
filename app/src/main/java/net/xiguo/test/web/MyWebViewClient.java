package net.xiguo.test.web;

import android.net.Uri;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import net.xiguo.test.BaseApplication;
import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by army on 2017/3/18.
 */

public class MyWebViewClient extends WebViewClient {
    private String h5Bridge = null;
    public MyWebViewClient() {
        super();
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
        LogUtil.i("shouldInterceptRequest: " + url);
        if(url.startsWith("http://www.army8735.me/")) {
            String path = url.substring(23);
            LogUtil.i("shouldInterceptPath: " + path);
            WebResourceResponse wrr = null;
            InputStream is = null;
            try {
//                InputStream is = BaseApplication.getContext().getResources().openRawResource(R.raw.test);
//                InputStream is = BaseApplication.getContext().getAssets().open("test.html");
                is = BaseApplication.getContext().openFileInput(path);
                if(path.endsWith(".html")) {
                    wrr = new WebResourceResponse("text/html", "utf-8", is);
                }
                else if(path.endsWith(".css")) {
                    wrr = new WebResourceResponse("text/css", "utf-8", is);
                }
                else if(path.endsWith(".js")) {
                    wrr = new WebResourceResponse("application/javascript", "utf-8", is);
                }
                else if(path.endsWith(".png")) {
                    wrr = new WebResourceResponse("image/png", "utf-8", is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return wrr;
        }
        return null;
    }

    @Override
    public void onPageFinished(WebView view, String args) {
        if(h5Bridge != null) {
            view.loadUrl("javascript:" + h5Bridge);
            return;
        }
        InputStream is = null;
        try {
            is = BaseApplication.getContext().getAssets().open("h5_bridge.js");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            h5Bridge = sb.toString();
            LogUtil.i("javascript:" + h5Bridge);
            view.loadUrl("javascript:" + h5Bridge);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
