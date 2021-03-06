package cc.circling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.push.CommonCallback;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.circling.utils.ImgUtil;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.MyWebChromeClient;
import cc.circling.web.MyWebViewClient;
import cc.circling.web.OkHttpDns;
import cc.circling.web.PreferenceEnum;
import cc.circling.web.SwipeRefreshLayout;
import cc.circling.web.URLs;
import cc.circling.web.WebView;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.MODE_PRIVATE;

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
    private MyWebChromeClient webChromeClient;

    private boolean hasCreateView = false;
    private boolean shouldLoad = false;
    private boolean hasLoad = false;
    private boolean shouldEnter = false;
    private boolean hasEnter = false;
    private String url;
    private Bundle bundle;
    private long lastT;
    private boolean isTransparentTitle;
    private String titleBgColor;
    private int titleBgAlpha = 0;

    private String loginWeiboClientId;
    private String confirmClientId;
    private String promptClientId;
    private String shareWbClientId;
    private String localMediaListClientId;
    private String deleteLocalMediaClientId;

    @Override
    public void onAttach(Context context) {
        LogUtil.i("onAttach");
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
    @Override
    public synchronized View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i("onCreateView", hasEnter + ", " + hasLoad + ", " + shouldEnter);
        hasCreateView = true;
        View view = inflater.inflate(R.layout.web_fragment, container, false);
        rootView = (FrameLayout) view;
        rootView.setVisibility(View.GONE);
        init();
        if(hasLoad && shouldEnter) {
            this.enterOnly();
        }
        else if(shouldLoad) {
            this.load(url, bundle);
        }
        return view;
    }
    @Override
    public void onDestroyView() {
        LogUtil.i("onDestroyView", url);
        super.onDestroyView();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.setSwipeRefreshLayout(null);
        webView.clearHistory();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        webView = null;
        MobclickAgent.onPageEnd(url);
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

        WebSettings webSettings = webView.getSettings();
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " app/ZhuanQuan/" + BuildConfig.VERSION_NAME);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        webView.setDrawingCacheEnabled(true);

        MyWebViewClient webViewClient = new MyWebViewClient();
        webView.setWebViewClient(webViewClient);
        webChromeClient = new MyWebChromeClient(this, mainActivity);
        webView.setWebChromeClient(webChromeClient);
        webView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        webView.setFragment(this);
        webView.setSwipeRefreshLayout(swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtil.i("swipeRefreshLayout onRefresh");
                // TODO: 极低概率下ZhuanQuanJSBridge还没有加载出来，进入卡死状态
                webView.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.trigger('refresh');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });
        webView.addJavascriptInterface(new ZhuanQuanJsBridgeNative(), "ZhuanQuanJsBridgeNative");
    }
    public synchronized void load(String url, Bundle bundle) {
        LogUtil.i("load", url + ", " + hasCreateView + ", " + shouldEnter);
        this.url = url;
        // 存在极端情况，添加fragment的transaction异步尚未执行，enter先执行了，需记录等待添加后执行
        if(!hasCreateView) {
            this.bundle = bundle;
            shouldLoad = true;
            return;
        }
        if(hasLoad) {
            return;
        }
        hasLoad = true;

        // 获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if(resourceId > 0) {
            statusBarHeight = this.getResources().getDimensionPixelSize(resourceId);
            LogUtil.d("statusBarHeight", statusBarHeight + "");
        }

        // 标题栏是否为透明
        String transparentTitle = bundle.getString("transparentTitle");
        isTransparentTitle = transparentTitle != null && transparentTitle.equals("true");
        LogUtil.d("transparentTitle ", transparentTitle + "");
        if(isTransparentTitle) {
            title.setTextColor(Color.parseColor("#FFFFFF"));
            title.setShadowLayer(4, 0, 2, Color.parseColor("#66000000"));
            subTitle.setTextColor(Color.parseColor("#FFFFFF"));
            subTitle.setShadowLayer(4, 0, 2, Color.parseColor("#66000000"));
            back.setImageResource(R.drawable.back_transparent);
            setTitleBgColor("transparent");
            titleBgColor = bundle.getString("titleBgColor");
            if(titleBgColor != null) {
                if(titleBgColor.startsWith("#")) {
                    titleBgColor = titleBgColor.substring(1);
                }
                if(titleBgColor.length() == 8) {
                    titleBgAlpha = Integer.parseInt(titleBgColor.substring(0, 2), 16);
                    titleBgAlpha = Math.max(255, titleBgAlpha);
                    titleBgAlpha = Math.min(0, titleBgAlpha);
                    titleBgColor = titleBgColor.substring(2);
                }
                else {
                    titleBgAlpha = 255;
                }
            }
        }
        else {
            titleBar.setPadding(0, statusBarHeight, 0, 0);
            float scale = this.getResources().getDisplayMetrics().density;
            LogUtil.d("scale", scale + "," + (int)(scale * 64));
            swipeRefreshLayout.setPadding(0, (int)(scale * 64), 0, 0);

            // titleBgColor
            String titleBgColor = bundle.getString("titleBgColor");
            LogUtil.d("titleBgColor", titleBgColor);
            if(titleBgColor != null && !titleBgColor.isEmpty()) {
                setTitleBgColor(titleBgColor);
            }
            else {
                setTitleBgColor("#FFFFFF");
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                titleBar.setElevation(8);
            }
        }

        // webview背景色
        String backgroundColor = bundle.getString("backgroundColor");
        LogUtil.d("backgroundColor", backgroundColor);
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
        LogUtil.d("title", sTitle);
        LogUtil.d("subTitle", sSubTitle);
        if(sTitle != null && sTitle.length() > 0) {
            title.setText(sTitle);
        }
        if(sSubTitle != null && sSubTitle.length() > 0) {
            subTitle.setText(sSubTitle);
            subTitle.setVisibility(View.VISIBLE);
        }

        // titleColor
        String titleColor = bundle.getString("titleColor");
        LogUtil.d("titleColor", titleColor);
        if(titleColor != null && titleColor.length() > 0) {
            int color = Color.parseColor(titleColor);
            LogUtil.i("titleColor ", color + "");
            title.setTextColor(color);
            subTitle.setTextColor(color);
        }

        // 是否隐藏back键
        String hideBackButton = bundle.getString("hideBackButton");
        LogUtil.d("hideBackButton ", hideBackButton);
        if(hideBackButton != null && hideBackButton.equals("true")) {
            back.setVisibility(View.GONE);
        }
        else {
            back.setVisibility(View.VISIBLE);
        }

        // 自定义back图片base64
        String backIcon = bundle.getString("backIcon");
        LogUtil.d("backIcon ", backIcon);
        if(backIcon != null && !backIcon.isEmpty()) {
            setBackIcon(backIcon);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click back");
                evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.trigger('back');");
            }
        });

        // optionMenu文字
        String optionMenu = bundle.getString("optionMenu");
        String optionMenuColor = bundle.getString("optionMenuColor");
        String optionMenuIcon1 = bundle.getString("optionMenuIcon1");
        String optionMenuIcon2 = bundle.getString("optionMenuIcon2");
        LogUtil.d("optionMenu", optionMenu);
        LogUtil.d("optionMenuIcon1", optionMenuIcon1);
        LogUtil.d("optionMenuIcon2", optionMenuIcon2);
        setOptionMenuText(optionMenu, optionMenuColor);
        setOptionMenuIcon(optionMenuIv1, optionMenuIcon1);
        setOptionMenuIcon(optionMenuIv2, optionMenuIcon2);

        optionMenuText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuText");
                evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('optionMenu');");
            }
        });
        optionMenuIv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuIv1");
                evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('optionMenu1');");
            }
        });
        optionMenuIv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuIv");
                evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('optionMenu2');");
            }
        });

        // 离线包地址添加cookie
        mainActivity.syncCookie(webView);
        if(url != null && url.length() > 0) {
            webView.loadUrl(url);
        }
        else {
            webView.loadUrl("about:blank");
        }
        MobclickAgent.onPageStart(url);

        if(shouldEnter) {
            enterOnly();
        }
    }
    public synchronized void enterOnly() {
        LogUtil.i("enterOnly", url + ", " + hasCreateView + ", " + hasLoad + ", " + hasEnter);
        shouldEnter = true;
        if(!hasCreateView || !hasLoad || hasEnter) {
            return;
        }
        hasEnter = true;
        rootView.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(MainActivity.WIDTH,0,0,0);
        translateAnimation.setDuration(300);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                WebFragment.this.mainActivity.prepare();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.startAnimation(translateAnimation);
    }
    public void enter(String url, Bundle bundle) {
        this.url = url;
        LogUtil.i("enter", url + ", " + hasCreateView + ", " + hasLoad);
        load(url, bundle);
        enterOnly();
    }
    public void setTitleBgColor(String backgroundColor) {
        if(backgroundColor.equals("transparent")) {
            backgroundColor = "#00000000";
        }
        int color = Color.parseColor(backgroundColor);
        LogUtil.d("backgroundColor ", color + "");
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
        LogUtil.d("setBackIcon", img);
        Bitmap bitmap = ImgUtil.parseBase64(img);
        back.setImageBitmap(bitmap);
    }
    public void setTitle(String s) {
        LogUtil.d("setTitle: " + s);
        // 有可能没有titleBar
        if(title != null) {
            title.setText(s);
        }
    }
    public void setSubTitle(String s) {
        LogUtil.d("setSubTitle: " + s);
        // 有可能没有titleBar
        if(subTitle != null) {
            if(s != null && !s.isEmpty()) {
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
    public void back() {
        evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.trigger('back');");
    }
    public void evaluateJavascript(String value) {
        if(value == null || value.isEmpty() || webView == null) {
            return;
        }
        webView.evaluateJavascript(value, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }
    public void remove() {
        webView.onPause();
        TranslateAnimation translateAnimation = new TranslateAnimation(0, MainActivity.WIDTH,0,0);
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
                WebFragment.this.resume();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(-50, 0,0,0);
        translateAnimation.setDuration(300);
        rootView.startAnimation(translateAnimation);
    }
    public void hide() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, -50,0,0);
        translateAnimation.setDuration(300);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rootView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.startAnimation(translateAnimation);
    }
    public void loginWeiboSuccess(String openId, String token) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("openID", openId); // TODO: 废除
        json.put("openId", openId);
        json.put("token", token);
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + loginWeiboClientId + "', " + json.toJSONString() + ");");
    }
    public void loginWeiboCancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 0);
        json.put("message", "取消授权");
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + loginWeiboClientId + "', " + json.toJSONString() + ");");
    }
    public void loginWeiboError(String message) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 1);
        json.put("message", message);
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + loginWeiboClientId + "', " + json.toJSONString() + ");");
    }
    public void confirm(boolean res) {
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + confirmClientId + "', " + res + ");");
    }
    public void hideBackButton() {
        back.setVisibility(View.GONE);
    }
    public void showBackButton() {
        back.setVisibility(View.VISIBLE);
    }
    public void prompt(JSONObject data) {
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + promptClientId + "', " + data.toJSONString() + ");");
    }
    public void popWindow(JSONObject data) {
        if(data == null) {
            evaluateJavascript("ZhuanQuanJsBridge.emit('resume');");
        }
        else {
            evaluateJavascript("ZhuanQuanJsBridge.emit('resume', " + data.toJSONString() + ");");
        }
    }
    public void resume() {
        webView.onResume();
        evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('resume');");
        MobclickAgent.onPageStart(url);
    }
    public void pause() {
        evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('pause');");
        webView.onPause();
        MobclickAgent.onPageEnd(url);
    }
    private void setOptionMenuText(String value, String color) {
        if(value != null && !value.isEmpty()) {
            optionMenuText.setText(value);
            optionMenuText.setVisibility(View.VISIBLE);
            if(color == null || color.isEmpty()) {
                color = "#636365";
            }
            int c = Color.parseColor(color);
            optionMenuText.setTextColor(c);
        }
        else {
            optionMenuText.setVisibility(View.GONE);
        }
    }
    private void setOptionMenuIcon(ImageView iv, String value) {
        if(value != null && !value.isEmpty()) {
            Bitmap bitmap = ImgUtil.parseBase64(value);
            iv.setImageBitmap(bitmap);
            iv.setVisibility(View.VISIBLE);
        }
        else {
            iv.setVisibility(View.GONE);
        }
    }
    public void albumOk(List<Uri> list) {
        webChromeClient.fileChooserCallback(list);
    }
    public void onScrollChanged(long t) {
        if(titleBgColor == null || titleBgAlpha == 0) {
            return;
        }
        t -= 64;
        if(t < 0) {
            t = 0;
        }
        t /= 4;
        if(t > 255) {
            t = 255;
        }
        if(t == lastT) {
            return;
        }
        lastT = t;
        String tp = Integer.toHexString((int)t * titleBgAlpha / 255);
        LogUtil.v("tp", tp);
        if(tp.length() == 1) {
            tp = "#0" + tp;
        }
        else {
            tp = "#" + tp;
        }
        setTitleBgColor(tp + titleBgColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float elevation = (int)t / 32;
            titleBar.setElevation(Math.max(elevation, 8));
        }
    }
    public void onWbShareSuccess() {
        JSONObject json = new JSONObject();
        json.put("success", true);
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + shareWbClientId + "', " + json.toString() + ");");
    }
    public void onWbShareFail() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + shareWbClientId + "', " + json.toString() + ");");
    }
    public void onWbShareCancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("cancel", true);
        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + shareWbClientId + "', " + json.toString() + ");");
    }
    public void fullScreen(View view) {
        altFullScreen();
        web.setVisibility(View.GONE);
        fullScreenView.setVisibility(View.VISIBLE);
        fullScreenView.addView(view);
    }
    public void unFullScreen() {
        altFullScreen();
        fullScreenView.removeAllViews();
        fullScreenView.setVisibility(View.GONE);
        web.setVisibility(View.VISIBLE);
        evaluateJavascript("ZhuanQuanJsBridge.trigger('unFullscreen');");
    }
    private void altFullScreen() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public void localMediaList(JSONArray list) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + localMediaListClientId + "', " + list.toString() + ");");
            }
        });
    }
    public void deleteLocalMedia(JSONObject json) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + deleteLocalMediaClientId + "', " + json.toString() + ");");
            }
        });
    }

    class ZhuanQuanJsBridgeNative extends Object {
        @JavascriptInterface
        public void call(String clientId, String key, String msg) {
            LogUtil.i("call", clientId + ", " + key + ", " + msg);
            switch(key) {
                case "alert":
                    alert(msg);
                    break;
                case "back":
                    back(msg);
                    break;
                case "confirm":
                    confirm(clientId, msg);
                    break;
                case "deleteLocalMedia":
                    deleteLocalMedia(clientId, msg);
                    break;
                case "download":
                    download(msg);
                    break;
                case "getCache":
                case "getPreference":
                    getCache(clientId, msg);
                    break;
                case "hideBackButton":
                    hideBackButton();
                    break;
                case "hideLoading":
                    hideLoading();
                    break;
                case "localMediaList":
                    localMediaList(clientId, msg);
                    break;
                case "login":
                    login(clientId, msg);
                    break;
                case "loginOut":
                    loginOut(clientId);
                    break;
                case "loginWeibo":
                    loginWeibo(clientId);
                    break;
                case "media":
                    media(clientId, msg);
                    break;
                case "moveTaskToBack":
                    moveTaskToBack();
                    break;
                case "networkInfo":
                    networkInfo(clientId);
                    break;
                case "notify":
                    notify(msg);
                    break;
                case "openUri":
                    openUri(msg);
                    break;
                case "popWindow":
                    popWindow(msg);
                    break;
                case "prompt":
                    prompt(clientId, msg);
                    break;
                case "pushWindow":
                    pushWindow(msg);
                    break;
                case "refresh":
                    refresh(msg);
                    break;
                case "refreshState":
                    refreshState(msg);
                    break;
                case "setBack":
                    setBack(msg);
                    break;
                case "setCache":
                case "setPreference":
                    setCache(clientId, msg);
                    break;
                case "setOptionMenu":
                    setOptionMenu(msg);
                    break;
                case "setSubTitle":
                    setSubTitle(msg);
                    break;
                case "setTitle":
                    setTitle(msg);
                    break;
                case "setTitleBgColor":
                    setTitleBgColor(msg);
                    break;
                case "shareWb":
                    shareWb(clientId, msg);
                    break;
                case "showBackButton":
                    showBackButton();
                    break;
                case "showLoading":
                    showLoading(msg);
                    break;
                case "toast":
                    toast(msg);
                    break;
            }
        }

        private void alert(String msg) {
            JSONObject json = JSON.parseObject(msg);
            String title = json.getString("title");
            String message = json.getString("message");
            mainActivity.alert(title, message);
        }
        private void back(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(webView == null) {
                        return;
                    }
                    JSONObject data = JSONObject.parseObject(msg);
                    boolean prevent = data.getBoolean("prevent");
                    if(!prevent) {
                        if(webView.canGoBack()) {
                            webView.goBack();
                        }
                        else {
                            mainActivity.back();
                        }
                    }
                }
            });
        }
        private void confirm(String clientId, String msg) {
            confirmClientId = clientId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = JSON.parseObject(msg);
                    String title = json.getString("title");
                    String message = json.getString("message");
                    mainActivity.confirm(title, message);
                }
            });
        }
        private void deleteLocalMedia(String clientId, String msg) {
            deleteLocalMediaClientId = clientId;
            JSONObject data = JSONObject.parseObject(msg);
            String name = data.getString("name");
            mainActivity.deleteLocalMedia(name);
        }
        private void download(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = JSON.parseObject(msg);
                    String url = json.getString("url");
                    String name = json.getString("name");
                    String title = json.getString("title");
                    int kind = json.getIntValue("kind");
                    if(title == null || title.isEmpty()) {
                        title = name;
                    }
                    mainActivity.download(url, name, kind, title);
                }
            });
        }
        private void getCache(String clientId, String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = JSON.parseObject(msg);
                    Boolean isArray = json.getBoolean("isArray");
                    if(isArray == null) {
                        isArray = false;
                    }
                    SharedPreferences sharedPreferences = BaseApplication
                            .getContext()
                            .getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE);
                    if(isArray) {
                        JSONArray key = json.getJSONArray("key");
                        if(key.size() > 0) {
                            StringBuilder value = new StringBuilder("[");
                            for(int i = 0; i < key.size(); i++) {
                                String k = key.getString(i);
                                String v = sharedPreferences.getString(k, "null");
                                value.append(v);
                                if(i < key.size() - 1) {
                                    value.append(",");
                                }
                            }
                            value.append("]");
                            evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "', " + value.toString() + ");");
                        }
                    }
                    else {
                        String key = json.getString("key");
                        String value = sharedPreferences.getString(key, "null");
                        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "', " + value + ");");
                    }
                }
            });
        }
        private void hideBackButton() {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebFragment.this.hideBackButton();
                }
            });
        }
        private void hideLoading() {
            mainActivity.hideLoading();
        }
        private void localMediaList(String clientId, String msg) {
            JSONObject json = JSON.parseObject(msg);
            int kind = json.getIntValue("kind");
            localMediaListClientId = clientId;
            switch(kind) {
                case 1:
                case 2:
                    mainActivity.localMediaList(kind);
                    break;
            }
        }
        private void login(String clientId, String msg) {
            JSONObject json = JSON.parseObject(msg);
            String url = json.getString("url");
            if(url != null && url.length() > 0) {
                OkHttpClient client = new OkHttpClient
                        .Builder()
                        .dns(OkHttpDns.getInstance())
                        .cookieJar(new CookieJar() {
                            @Override
                            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                SharedPreferences.Editor editor = mainActivity
                                        .getSharedPreferences(PreferenceEnum.SESSION.name(), Context.MODE_PRIVATE).edit();
                                for(Cookie cookie : cookies) {
                                    LogUtil.i("cookie string", cookie.toString());
                                    MyCookies.add(cookie.name(), cookie.toString());
                                    editor.putString(cookie.name(), cookie.toString());
                                }
                                editor.apply();
                            }

                            @Override
                            public List<Cookie> loadForRequest(HttpUrl url) {
                                return new ArrayList<>();
                            }
                        })
                        .build();
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                JSONObject data = json.getJSONObject("data");
                if(data != null) {
                    Set<String> keys = data.keySet();
                    for(String key : keys) {
                        String value = data.getString(key);
                        bodyBuilder.add(key, value);
                        LogUtil.i("data: " + key + ", " + value);
                    }
                }
                RequestBody requestBody = bodyBuilder.build();
                String fullUrl = url;
                if(!fullUrl.startsWith("http")) {
                    fullUrl = URLs.H5_DOMAIN + url;
                }
                Request request = new Request.Builder()
                        .addHeader("origin", URLs.WEB_DOMAIN)
                        .url(fullUrl)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    ResponseBody body = response.body();
                    final String responseBody = body == null ? "" : body.string();
                    LogUtil.i("responseBody: " + responseBody);
                    if(!responseBody.isEmpty()) {
                        JSONObject res = JSONObject.parseObject(responseBody);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.syncCookie();
                                evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "', " + responseBody + ");");
                            }
                        });
                        if(res.getBoolean("success")) {
                            JSONObject user = res.getJSONObject("data").getJSONObject("user");
                            String uid = user.getString("id");
                            MobclickAgent.onProfileSignIn(uid);
                            CrashReport.setUserId(uid);
                            BaseApplication.getCloudPushService().bindAccount(uid, new CommonCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    LogUtil.i("bindAccount success", message);
                                }

                                @Override
                                public void onFailed(String message, String arg) {
                                    LogUtil.i("bindAccount fail", message + ", " + arg);
                                }
                            });
                        }
                        return;
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = new JSONObject();
                        json.put("success", false);
                        evaluateJavascript("ZhuanQuanJSBridge._invokeJs('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        private void loginOut(String clientId) {
            SharedPreferences sharedPreferences = mainActivity
                    .getSharedPreferences(PreferenceEnum.SESSION.name(), MODE_PRIVATE);
            SharedPreferences.Editor editor = mainActivity
                    .getSharedPreferences(PreferenceEnum.SESSION.name(), MODE_PRIVATE).edit();
            Map<String, ?> map = sharedPreferences.getAll();
            for(String key : map.keySet()) {
                LogUtil.i("remove", key);
                MyCookies.remove(key);
                editor.remove(key);
            }
            editor.apply();
            MobclickAgent.onProfileSignOff();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.syncCookie();
                    evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "');");
                }
            });
        }
        private void loginWeibo(String clientId) {
            loginWeiboClientId = clientId;
            mainActivity.loginWeibo();
        }
        private void media(String clientId, String msg) {
            if(msg == null || msg.isEmpty()) {
                return;
            }
            JSONObject data = JSON.parseObject(msg);
            String key = data.getString("key");
            JSONObject value = data.getJSONObject("value");
            mainActivity.media(clientId, key, value);
        }
        private void moveTaskToBack() {
            mainActivity.moveTaskToBack(true);
        }
        private void networkInfo(String clientId) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if(networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
                        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        JSONObject json = new JSONObject();
                        json.put("available", true);
                        json.put("wifi", wifiNetworkInfo != null & wifiNetworkInfo.isConnected() && wifiNetworkInfo.isAvailable());
                        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "', " + json.toJSONString() + ");");
                    }
                    else {
                        JSONObject json = new JSONObject();
                        json.put("available", false);
                        evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "', " + json.toJSONString() + ");");
                    }
                }
            });
        }
        private void notify(String msg) {
            JSONObject json = JSON.parseObject(msg);
            JSONObject data = json.getJSONObject("data");
            String ticker = data.getString("ticker");
            String title = data.getString("title");
            String content = data.getString("content");
            if(title == null) {
                title = "";
            }
            if(ticker == null || ticker.equals("")) {
                ticker = title;
            }
            mainActivity.notify(ticker, title, content);
        }
        private void openUri(String msg) {
            String value = (String) JSON.parse(msg);
            if(!value.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(value);
                intent.setData(uri);
                mainActivity.startActivity(intent);
            }
        }
        private void popWindow(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = msg == null || msg.isEmpty() ? null : JSON.parseObject(msg);
                    mainActivity.popWindow(data);
                }
            });
        }
        private void prompt(String clientId, String msg) {
            promptClientId = clientId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = JSON.parseObject(msg);
                    String title = data.getString("title");
                    String message = data.getString("message");
                    String value = data.getString("value");
                    mainActivity.prompt(title, message, value);
                }
            });
        }
        private void pushWindow(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = JSON.parseObject(msg);
                    mainActivity.pushWindow(data);
                    WebFragment.this.hide();
                }
            });
        }
        private void refresh(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = JSONObject.parseObject(msg);
                    boolean prevent = data.getBoolean("prevent");
                    if(!prevent) {
                        webView.reload();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        private void refreshState(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean value = (boolean) JSON.parse(msg);
                    swipeRefreshLayout.setCanEnabled(value);
                }
            });
        }
        private void setBack(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = JSON.parseObject(msg);
                    String base64 = data.getString("img");
                    setBackIcon(base64);
                }
            });
        }
        private void setCache(String clientId, String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = JSON.parseObject(msg);
                    Boolean isArray = json.getBoolean("isArray");
                    if(isArray == null) {
                        isArray = false;
                    }
                    SharedPreferences.Editor editor = BaseApplication
                            .getContext()
                            .getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE).edit();
                    if(isArray) {
                        JSONArray key = json.getJSONArray("key");
                        JSONArray value = json.getJSONArray("value");
                        if(key.size() > 0) {
                            for (int i = 0; i < key.size(); i++) {
                                String k = key.getString(i);
                                String v = value.getString(i);
                                LogUtil.i("setCache value is null: " + (v == null));
                                editor.remove(k);
                                if(v != null) {
                                    editor.putString(k, v);
                                }
                            }
                            editor.apply();
                        }
                    }
                    else {
                        String key = json.getString("key");
                        String value = json.getString("value");
                        editor.remove(key);
                        if(value != null) {
                            editor.putString(key, value);
                        }
                        editor.apply();
                    }
                    evaluateJavascript("ZhuanQuanJsBridge._invokeJs('" + clientId + "');");
                }
            });
        }
        private void setOptionMenu(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = JSON.parseObject(msg);
                    String text = json.getString("text");
                    String textColor = json.getString("textColor");
                    if(textColor == null || textColor.isEmpty()) {
                        textColor = "#000000";
                    }
                    String img1 = json.getString("icon1");
                    String img2 = json.getString("icon2");
                    setOptionMenuText(text, textColor);
                    setOptionMenuIcon(optionMenuIv1, img1);
                    setOptionMenuIcon(optionMenuIv2, img2);
                }
            });
        }
        private void setSubTitle(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = (String) JSON.parse(msg);
                    WebFragment.this.setSubTitle(value);
                }
            });
        }
        private void setTitle(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = (String) JSON.parse(msg);
                    WebFragment.this.setTitle(value);
                }
            });
        }
        private void setTitleBgColor(String msg) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = (String) JSON.parse(msg);
                    WebFragment.this.setTitleBgColor(value);
                }
            });
        }
        private void shareWb(String clientId, String msg) {
            shareWbClientId = clientId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.shareWb(JSON.parseObject(msg));
                }
            });
        }
        private void showBackButton() {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebFragment.this.showBackButton();
                }
            });
        }
        private void showLoading(String msg) {
            JSONObject json = JSON.parseObject(msg);
            String title = json.getString("title");
            String message = json.getString("message");
            boolean cancelable = json.getBoolean("cancelable");
            mainActivity.showLoading(title, message, cancelable);
        }
        private void toast(String msg) {
            String value = (String) JSON.parse(msg);
            Toast toast = Toast.makeText(mainActivity, value, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
