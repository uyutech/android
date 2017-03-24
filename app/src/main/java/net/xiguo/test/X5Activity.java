package net.xiguo.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import net.xiguo.test.event.H5EventDispatcher;
import net.xiguo.test.plugin.H5Plugin;
import net.xiguo.test.plugin.SetTitlePlugin;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyWebChromeClient;
import net.xiguo.test.web.MyWebViewClient;

/**
 * Created by army on 2017/3/16.
 */

public class X5Activity extends AppCompatActivity {
    public static final String DOMAIN = "http://www.army8735.me/";

    private SetTitlePlugin setTitlePlugin;

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
        webView.loadUrl(DOMAIN + "index.html");
    }

    private void initPlugins() {
        setTitlePlugin = new SetTitlePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE, setTitlePlugin);
    }

    public void setTitle(String title) {
        LogUtil.i("setTitle: " + title);
        TextView tv = (TextView) findViewById(R.id.webViewTitle);
        tv.setText(title);
    }
}
