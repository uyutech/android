package net.xiguo.test;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebView;

import net.xiguo.test.event.H5EventDispatcher;
import net.xiguo.test.plugin.H5Plugin;
import net.xiguo.test.plugin.PushWindowPlugin;
import net.xiguo.test.plugin.SetTitlePlugin;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.MyWebChromeClient;
import net.xiguo.test.web.MyWebViewClient;
import net.xiguo.test.web.URLs;

/**
 * Created by army on 2017/3/16.
 */

public class X5Activity extends AppCompatActivity {

    private SetTitlePlugin setTitlePlugin;
    private PushWindowPlugin pushWindowPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_x5);

        initPlugins();

        WebView webView = (WebView) findViewById(R.id.x5);
        webView.getSettings().setJavaScriptEnabled(true);

        MyWebViewClient webViewClient = new MyWebViewClient(this);
        webView.setWebViewClient(webViewClient);
        MyWebChromeClient webChromeClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(webChromeClient);

        // 从上个启动活动获取需要加载的url
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        LogUtil.i("url: " + url);

        // 离线包地址添加cookie
        if(url.startsWith(URLs.H5_DOMAIN)) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            for (String s : MyCookies.getAll()) {
                cookieManager.setCookie(url, s);
            }
            if (Build.VERSION.SDK_INT < 21) {
                CookieSyncManager.getInstance().sync();
            } else {
                CookieManager.getInstance().flush();
            }
        }
        webView.loadUrl(url);
    }

    private void initPlugins() {
        setTitlePlugin = new SetTitlePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE, setTitlePlugin);
        pushWindowPlugin = new PushWindowPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.PUSH_WINDOW, pushWindowPlugin);
    }

    public void setTitle(String title) {
        LogUtil.i("setTitle: " + title);
        TextView tv = (TextView) findViewById(R.id.webViewTitle);
        tv.setText(title);
    }
    public void pushWindow(String url) {
        LogUtil.i("pushWindow: " + url);
        Intent intent = new Intent(X5Activity.this, X5Activity.class);
        intent.putExtra("url", url);
        startActivityForResult(intent, 1);
    }
}
