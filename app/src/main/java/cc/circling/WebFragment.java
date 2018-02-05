package cc.circling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

import cc.circling.utils.ImgUtil;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.MyWebChromeClient;
import cc.circling.web.MyWebViewClient;
import cc.circling.web.SwipeRefreshLayout;
import cc.circling.web.URLs;
import cc.circling.web.WebView;

/**
 * Created by army8735 on 2018/1/30.
 */

public class WebFragment extends Fragment {
    private MainActivity mainActivity;

    private FrameLayout rootView;
    private LinearLayout titleBar;
    private TextView title;
    private TextView subTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView back;
    private WebView webView;
    private TextView optionMenuText;
    private ImageView optionMenuIv1;
    private ImageView optionMenuIv2;
    private FrameLayout web;
    private FrameLayout fullScreenView;
    private View mask;

    private boolean hasCreateView = false;
    private boolean hasEnter = false;
    private String url;
    private Bundle bundle;

    private static CookieManager cookieManager;

    @Override
    public void onAttach(Context context) {
        LogUtil.i("onAttach");
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
    @Override
    public synchronized View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i("onCreateView", hasEnter + "");
        hasCreateView = true;
        View view = inflater.inflate(R.layout.web_fragment, container, false);
        rootView = (FrameLayout) view;
        init();
        if(hasEnter) {
            this.enter(this.url, this.bundle);
        }
        return view;
    }
    @Override
    public void onStart() {
        LogUtil.i("onStart");
        super.onStart();
    }
    @Override
    public void onDestroyView() {
        LogUtil.i("onDestroyView");
        super.onDestroyView();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.setSwipeRefreshLayout(null);
        webView.clearHistory();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        webView = null;
    }
    @Override
    public void onDestroy() {
        LogUtil.i("onDestroy");
        super.onDestroy();
    }

    private void init() {
        titleBar = rootView.findViewById(R.id.titleBar);
        title = rootView.findViewById(R.id.title);
        subTitle = rootView.findViewById(R.id.subTitle);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        webView = rootView.findViewById(R.id.webView);
        back = rootView.findViewById(R.id.back);
        optionMenuText = rootView.findViewById(R.id.optionMenuText);
        optionMenuIv1 = rootView.findViewById(R.id.optionMenuIv1);
        optionMenuIv2 = rootView.findViewById(R.id.optionMenuIv2);
        web = rootView.findViewById(R.id.web);
        fullScreenView =  rootView.findViewById(R.id.fullScreen);
        mask = rootView.findViewById(R.id.mask);

        WebSettings webSettings = webView.getSettings();
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " app/ZhuanQuan/" + BuildConfig.VERSION_NAME);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);
        webView.setDrawingCacheEnabled(true);

        MyWebViewClient webViewClient = new MyWebViewClient();
        webView.setWebViewClient(webViewClient);
        MyWebChromeClient webChromeClient = new MyWebChromeClient(mainActivity);
        webView.setWebChromeClient(webChromeClient);
        webView.setWebContentsDebuggingEnabled(true);

        webView.setSwipeRefreshLayout(swipeRefreshLayout);
        webView.addJavascriptInterface(new ZhuanQuanJsBridgeNative(), "ZhuanQuanJsBridgeNative");
    }
    public synchronized void enter(String url, Bundle bundle) {
        LogUtil.i("enter", url + ", " + hasCreateView);
        // 存在极端情况，添加fragment的transacation异步尚未执行，enter先执行了，需记录等待添加后执行
        if(!hasCreateView) {
            this.url = url;
            this.bundle = bundle;
            hasEnter = true;
            return;
        }

        // 获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if(resourceId > 0) {
            statusBarHeight = this.getResources().getDimensionPixelSize(resourceId);
            LogUtil.i("statusBarHeight", statusBarHeight + "");
        }

        // 标题栏是否为透明
        String transparentTitle = bundle.getString("transparentTitle");
        LogUtil.i("transparentTitle ", transparentTitle + "");
        if(transparentTitle != null && transparentTitle.equals("true")) {
            title.setTextColor(Color.parseColor("#FFFFFF"));
            title.setShadowLayer(4, 0, 2, Color.parseColor("#33000000"));
            subTitle.setTextColor(Color.parseColor("#FFFFFF"));
            subTitle.setShadowLayer(4, 0, 2, Color.parseColor("#33000000"));
            back.setImageResource(R.drawable.back_transparent);
        }
        else {
            titleBar.setPadding(0, statusBarHeight, 0, 0);
            int titleBarHeight = titleBar.getHeight();
            float scale = this.getResources().getDisplayMetrics().density;
            LogUtil.i("scale", scale + "");
            swipeRefreshLayout.setPadding(0, (int)scale * 64, 0, 0);

            // titleBgColor
            String titleBgColor = bundle.getString("titleBgColor");
            LogUtil.i("titleBgColor", titleBgColor);
            if(titleBgColor != null && titleBgColor.length() > 0) {
                setTitleBgColor(titleBgColor);
            }
            else {
                setTitleBgColor("#FFFFFF");
            }
        }

        // webview背景色
        String backgroundColor = bundle.getString("backgroundColor");
        LogUtil.i("backgroundColor", backgroundColor);
        if(backgroundColor != null && backgroundColor.length() > 0) {
            int color = Color.parseColor(backgroundColor);
            webView.setBackgroundColor(color);
        }
        else {
            webView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        // title
        String sTitle = bundle.getString("title");
        String sSubTitle = bundle.getString("subTitle");
        LogUtil.i("title", sTitle);
        LogUtil.i("subTitle", sSubTitle);
        if(sTitle != null && sTitle.length() > 0) {
            title.setText(sTitle);
        }
        if(sSubTitle != null && sSubTitle.length() > 0) {
            subTitle.setText(sSubTitle);
            subTitle.setVisibility(View.VISIBLE);
        }

        // titleColor
        String titleColor = bundle.getString("titleColor");
        LogUtil.i("titleColor", titleColor);
        if(titleColor != null && titleColor.length() > 0) {
            int color = Color.parseColor(titleColor);
            LogUtil.i("titleColor ", color + "");
            title.setTextColor(color);
            subTitle.setTextColor(color);
        }

        // 是否隐藏back键
        String hideBackButton = bundle.getString("hideBackButton");
        LogUtil.i("hideBackButton ", hideBackButton);
        if(hideBackButton != null && hideBackButton.equals("true")) {
            back.setVisibility(View.GONE);
        }
        else {
            back.setVisibility(View.VISIBLE);
        }

        // 自定义back图片base64
        String backIcon = bundle.getString("backIcon");
        LogUtil.i("backIcon ", backIcon);
        if(backIcon != null && backIcon.length() > 0) {
            setBackIcon(backIcon);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click back");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.trigger('back');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });

        // optionMenu文字
        String optionMenu = bundle.getString("optionMenu");
        String optionMenuIcon1 = bundle.getString("optionMenuIcon1");
        String optionMenuIcon2 = bundle.getString("optionMenuIcon2");
        LogUtil.i("optionMenu", optionMenu);
        LogUtil.i("optionMenuIcon1", optionMenuIcon1);
        LogUtil.i("optionMenuIcon2", optionMenuIcon2);
        if(optionMenu != null && !optionMenu.equals("")) {
            optionMenuText.setText(optionMenu);
            optionMenuText.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuText.setVisibility(View.GONE);
        }
        if(optionMenuIcon1 != null && optionMenuIcon1.length() > 0) {
            Bitmap bitmap = ImgUtil.parseBase64(optionMenuIcon1);
            optionMenuIv1.setImageBitmap(bitmap);
            optionMenuIv1.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv1.setVisibility(View.GONE);
        }
        if(optionMenuIcon2 != null && optionMenuIcon2.length() > 0) {
            Bitmap bitmap = ImgUtil.parseBase64(optionMenuIcon2);
            optionMenuIv2.setImageBitmap(bitmap);
            optionMenuIv2.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv2.setVisibility(View.GONE);
        }

        optionMenuText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuText");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('optionMenu');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });
        optionMenuIv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuIv1");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('optionMenu1');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });
        optionMenuIv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuIv");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('optionMenu2');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });

        // 离线包地址添加cookie
//        syncCookie();
        if(url != null && url.length() > 0) {
            webView.loadUrl(url);
        }
        else {
            webView.loadUrl("about:blank");
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(MainActivity.WIDTH,0,0,0);
        translateAnimation.setDuration(400);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rootView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.startAnimation(translateAnimation);
    }
    public void setTitleBgColor(String backgroundColor) {
        if(backgroundColor.equals("transparent")) {
            backgroundColor = "#00000000";
        }
        int color = Color.parseColor(backgroundColor);
        LogUtil.i("backgroundColor ", color + "");
        titleBar.setBackgroundColor(color);
        // 透明则可点穿
        if(backgroundColor.length() == 9 && backgroundColor.substring(1, 3).equals("00")) {
            titleBar.setClickable(false);
        }
        else {
            titleBar.setClickable(true);
        }
    }
    public void setBackIcon(String img) {
        LogUtil.i("setBackIcon", img);
        Bitmap bitmap = ImgUtil.parseBase64(img);
        back.setImageBitmap(bitmap);
    }
    public void syncCookie() {
        LogUtil.i("syncCookie");
        CookieSyncManager.createInstance(BaseApplication.getContext());
        if(cookieManager == null) {
            cookieManager = CookieManager.getInstance();
        }

        cookieManager.setAcceptCookie(true);
        cookieManager.removeExpiredCookie();
        // 跨域CORS的ajax设置允许cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        HashMap<String, String> hashMap = MyCookies.getAll();
        for(String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            LogUtil.i("CookieManager: ", key + ", " + value);
            cookieManager.setCookie(URLs.WEB_DOMAIN, value);
            cookieManager.setCookie(URLs.H5_DOMAIN, value);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.i("CookieSyncManager sync");
            CookieSyncManager.getInstance().sync();
        } else {
            LogUtil.i("CookieManager flush");
            CookieManager.getInstance().flush();
        }
    }
    public void setTitle(String s) {
        LogUtil.i("setTitle: " + s);
        // 有可能没有titleBar
        if(title != null) {
            title.setText(s);
        }
    }
    public void setSubTitle(String s) {
        LogUtil.i("setSubTitle: " + s);
        // 有可能没有titleBar
        if(subTitle != null) {
            if(s != null && s.length() > 0) {
                subTitle.setVisibility(View.VISIBLE);
            }
            else {
                subTitle.setVisibility(View.GONE);
            }
            subTitle.setText(s);
        }
    }
    public WebView getWebView() {
        return webView;
    }
    public View getView() {
        return rootView;
    }
    public void back() {
        webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.trigger('back');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }
    public void remove() {
        webView.onPause();
        TranslateAnimation translateAnimation = new TranslateAnimation(0,MainActivity.WIDTH,0,0);
        translateAnimation.setDuration(300);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainActivity.remove(WebFragment.this);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.startAnimation(translateAnimation);
    }
    public void show() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mask.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mask.startAnimation(alphaAnimation);
    }
    public void hide() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 0.8f);
        alphaAnimation.setDuration(400);
        alphaAnimation.setFillAfter(true);
        mask.startAnimation(alphaAnimation);
        mask.setVisibility(View.VISIBLE);
    }

    class ZhuanQuanJsBridgeNative extends Object {
        @JavascriptInterface
        public void call(String msg) {
            LogUtil.i("msg", msg);
            JSONObject json = JSON.parseObject(msg);
            String key = json.getString("key");
            JSONObject value = json.getJSONObject("value");
            switch(key) {
                case "pushWindow":
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.pushWindow(value);
                            WebFragment.this.hide();
                        }
                    });
                    break;
                case "back":
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(webView.canGoBack()) {
                                webView.goBack();
                            }
                            else {
                                mainActivity.back();
                            }
                        }
                    });
                    break;
            }
        }
    }
}
