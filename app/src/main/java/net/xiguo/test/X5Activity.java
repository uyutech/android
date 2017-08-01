package net.xiguo.test;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;
import net.xiguo.test.plugin.SwipeRefreshPlugin;
import net.xiguo.test.web.WebView;

import net.xiguo.test.event.H5EventDispatcher;
import net.xiguo.test.plugin.AlertPlugin;
import net.xiguo.test.plugin.BackPlugin;
import net.xiguo.test.plugin.ConfirmPlugin;
import net.xiguo.test.plugin.H5Plugin;
import net.xiguo.test.plugin.HideBackButtonPlugin;
import net.xiguo.test.plugin.HideLoadingPlugin;
import net.xiguo.test.plugin.PopWindowPlugin;
import net.xiguo.test.plugin.PushWindowPlugin;
import net.xiguo.test.plugin.SetTitlePlugin;
import net.xiguo.test.plugin.ShowBackButtonPlugin;
import net.xiguo.test.plugin.ShowLoadingPlugin;
import net.xiguo.test.plugin.ToastPlugin;
import net.xiguo.test.plugin.UserInfoPlugin;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.MyWebChromeClient;
import net.xiguo.test.web.MyWebViewClient;
import net.xiguo.test.web.URLs;
import net.xiguo.test.web.SwipeRefreshLayout;

/**
 * Created by army on 2017/3/16.
 */

public class X5Activity extends AppCompatActivity {

    private SetTitlePlugin setTitlePlugin;
    private PushWindowPlugin pushWindowPlugin;
    private PopWindowPlugin popWindowPlugin;
    private BackPlugin backPlugin;
    private ToastPlugin toastPlugin;
    private ShowLoadingPlugin showLoadingPlugin;
    private HideLoadingPlugin hideLoadingPlugin;
    private AlertPlugin alertPlugin;
    private ConfirmPlugin confirmPlugin;
    private HideBackButtonPlugin hideBackButtonPlugin;
    private ShowBackButtonPlugin showBackButtonPlugin;
    private UserInfoPlugin userInfoPlugin;
    private SwipeRefreshPlugin swipeRefreshPlugin;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView back;
    private WebView webView;
    private String url;

    private boolean firstWeb;
    private boolean firstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_x5_transparent);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        initPlugins();
        firstRun = true;

        webView = (WebView) findViewById(R.id.x5);
        WebSettings webSettings = webView.getSettings();
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " app/ZhuanQuan");
        webSettings.setJavaScriptEnabled(true);
        // 支持缩放viewport
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        IX5WebViewExtension ix5 = webView.getX5WebViewExtension();
        webView.setDrawingCacheEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        webView.setSwipeRefreshLayout(swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtil.i("swipeRefreshLayout onRefresh");
                webView.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // 关闭小窗播放和方块滚动条
        Bundle data = new Bundle();
        data.putBoolean("supportLiteWnd", false);
        if(null != ix5) {
            ix5.invokeMiscMethod("setVideoParams", data);
            ix5.setScrollBarFadingEnabled(false);
        }

        MyWebViewClient webViewClient = new MyWebViewClient(this);
        webView.setWebViewClient(webViewClient);
        MyWebChromeClient webChromeClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(webChromeClient);

        // 从上个启动活动获取需要加载的url
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        LogUtil.i("url: " + url);
        // 是否第一个web
        firstWeb = intent.getBooleanExtra("firstWeb", false);
        LogUtil.i("firstWeb: " + firstWeb);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click back");
                webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('back');");
            }
        });

        // 离线包地址添加cookie
        if(url.startsWith(URLs.H5_DOMAIN)) {
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();

            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                LogUtil.i("CookieSyncManager sync");
                CookieSyncManager.getInstance().sync();
            } else {
                LogUtil.i("CookieManager flush");
                CookieManager.getInstance().flush();
            }

            for (String s : MyCookies.getAll()) {
                LogUtil.i("CookieManager: ", url + ", " + s);
                cookieManager.setCookie(url, s);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                LogUtil.i("CookieSyncManager sync");
                CookieSyncManager.getInstance().sync();
            } else {
                LogUtil.i("CookieManager flush");
                CookieManager.getInstance().flush();
            }
        }
        webView.loadUrl(url);
    }

    private void initPlugins() {
//        setTitlePlugin = new SetTitlePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE, setTitlePlugin);
        pushWindowPlugin = new PushWindowPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.PUSH_WINDOW, pushWindowPlugin);

        popWindowPlugin = new PopWindowPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.POP_WINDOW, popWindowPlugin);

        backPlugin = new BackPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.BACK, backPlugin);

        toastPlugin = new ToastPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.TOAST, toastPlugin);

        showLoadingPlugin = new ShowLoadingPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SHOW_LOADING, showLoadingPlugin);

        hideLoadingPlugin = new HideLoadingPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.HIDE_LOADING, hideLoadingPlugin);

        alertPlugin = new AlertPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.ALERT, alertPlugin);

        confirmPlugin = new ConfirmPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.CONFIRM, confirmPlugin);

        hideBackButtonPlugin = new HideBackButtonPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.HIDE_BACKBUTTON, hideBackButtonPlugin);

        showBackButtonPlugin = new ShowBackButtonPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SHOW_BACKBUTTON, showBackButtonPlugin);

        userInfoPlugin = new UserInfoPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.USER_INFO, userInfoPlugin);

        swipeRefreshPlugin = new SwipeRefreshPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SWIPE_REFRESH, swipeRefreshPlugin);
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
    public boolean isFirstWeb() {
        return firstWeb;
    }
    public WebView getWebView() {
        return webView;
    }
    public void hideBackButton() {
        back.setVisibility(View.GONE);
    }
    public void showBackButton() {
        back.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtil.i("keyup: " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            LogUtil.i("KEYCODE_BACK");
            webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('back');");
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    String params = data.getStringExtra("params");
                    LogUtil.i("onActivityResult: " + params);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("onStart: ", url);
        if(!firstRun) {
            webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('resume');");
        }
        else {
            firstRun = false;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i("onStop: ", url);
        webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('pause');");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.clearHistory();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        webView = null;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
}
