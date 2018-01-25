package cc.circling;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cc.circling.login.oauth.Constants;
import cc.circling.plugin.AlbumPlugin;
import cc.circling.plugin.DownloadPlugin;
import cc.circling.plugin.GetCachePlugin;
import cc.circling.plugin.LoginOutPlugin;
import cc.circling.plugin.LoginPlugin;
import cc.circling.plugin.LoginWeiboPlugin;
import cc.circling.plugin.GetPreferencePlugin;
import cc.circling.plugin.MediaPlugin;
import cc.circling.plugin.MoveTaskToBackPlugin;
import cc.circling.plugin.NetworkInfoPlugin;
import cc.circling.plugin.NotifyPlugin;
import cc.circling.plugin.OpenUriPlugin;
import cc.circling.plugin.PromptPlugin;
import cc.circling.plugin.SetBackPlugin;
import cc.circling.plugin.SetCachePlugin;
import cc.circling.plugin.SetOptionMenuPlugin;
import cc.circling.plugin.SetPreferencePlugin;
import cc.circling.plugin.SetCookiePlugin;
import cc.circling.plugin.SetSubTitlePlugin;
import cc.circling.plugin.SetTitleBgColorPlugin;
import cc.circling.plugin.RefreshPlugin;
import cc.circling.plugin.RefreshStatePlugin;
import cc.circling.plugin.WeiboLoginPlugin;
import cc.circling.utils.AndroidBug5497Workaround;
import cc.circling.utils.ImgUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.WebView;

import cc.circling.event.H5EventDispatcher;
import cc.circling.plugin.AlertPlugin;
import cc.circling.plugin.BackPlugin;
import cc.circling.plugin.ConfirmPlugin;
import cc.circling.plugin.H5Plugin;
import cc.circling.plugin.HideBackButtonPlugin;
import cc.circling.plugin.HideLoadingPlugin;
import cc.circling.plugin.PopWindowPlugin;
import cc.circling.plugin.PushWindowPlugin;
import cc.circling.plugin.SetTitlePlugin;
import cc.circling.plugin.ShowBackButtonPlugin;
import cc.circling.plugin.ShowLoadingPlugin;
import cc.circling.plugin.ToastPlugin;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyWebChromeClient;
import cc.circling.web.MyWebViewClient;
import cc.circling.web.URLs;
import cc.circling.web.SwipeRefreshLayout;

/**
 * Created by army on 2017/3/16.
 */

public class X5Activity extends AppCompatActivity {
    public static final int PUSH_WINDOW_OK = 8735;
    public static final int REQUEST_ALBUM_OK = 8736;

    private SetTitlePlugin setTitlePlugin;
    private SetSubTitlePlugin setSubTitlePlugin;
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
    private RefreshPlugin refreshPlugin;
    private RefreshStatePlugin refreshStatePlugin;
    private LoginWeiboPlugin loginWeiboPlugin;
    private GetPreferencePlugin getPreferencePlugin;
    private SetPreferencePlugin setPreferencePlugin;
    private SetOptionMenuPlugin setOptionMenuPlugin;
    private SetTitleBgColorPlugin setTitleBgColorPlugin;
    private MoveTaskToBackPlugin moveTaskToBackPlugin;
    private OpenUriPlugin openUriPlugin;
    private SetCookiePlugin setCookiePlugin;
    private WeiboLoginPlugin weiboLoginPlugin;
    private LoginOutPlugin loginOutPlugin;
    private NotifyPlugin notificationPlugin;
    private AlbumPlugin albumPlugin;
    private PromptPlugin promptPlugin;
    private DownloadPlugin downloadPlugin;
    private NetworkInfoPlugin networkInfoPlugin;
    private LoginPlugin loginPlugin;
    private MediaPlugin mediaPlugin;
    private SetBackPlugin setBackPlugin;
    private SetCachePlugin setCachePlugin;
    private GetCachePlugin getCachePlugin;

    private LinearLayout titleBar;
    private TextView title;
    private TextView subTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView back;
    private WebView webView;
    private TextView optionMenuText;
    private ImageView optionMenuIv1;
    private ImageView optionMenuIv2;
    private ViewGroup web;
    private FrameLayout fullScreenView;

    private String url;
    private String popWindowParam;
    private boolean hasSetTitle = false;
    private boolean readTitle;

    private SsoHandler mSsoHandler;

    private static CookieManager cookieManager;

    private MediaService.PlayBinder playBinder;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setFormat(PixelFormat.TRANSLUCENT);

        Intent intent = getIntent();
        // 标题栏是否为透明
        String transparentTitle = intent.getStringExtra("transparentTitle");
        LogUtil.i("transparentTitle ", transparentTitle + "");
        if(transparentTitle != null && transparentTitle.equals("true")) {
            setContentView(R.layout.activity_x5_transparent);
        }
        else {
            setContentView(R.layout.activity_x5);
        }
        AndroidBug5497Workaround.assistActivity(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        titleBar = findViewById(R.id.titleBar);
        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.subTitle);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        webView = findViewById(R.id.x5);
        back = findViewById(R.id.back);
        optionMenuText = findViewById(R.id.optionMenuText);
        optionMenuIv1 = findViewById(R.id.optionMenuIv1);
        optionMenuIv2 = findViewById(R.id.optionMenuIv2);
        web = findViewById(R.id.web);
        fullScreenView =  findViewById(R.id.fullScreen);

        // 动态设置状态栏paddingTop
        int statusBarHeight = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            statusBarHeight = this.getResources().getDimensionPixelSize(resourceId);
            LogUtil.i("statusBarHeight", statusBarHeight + "");
            int paddingLeft = titleBar.getPaddingLeft();
            int paddingRight = titleBar.getPaddingRight();
            LogUtil.i("paddingLeftRight", paddingLeft + ", " + paddingRight);
            titleBar.setPadding(paddingLeft, statusBarHeight, paddingRight, 0);
            float scale = this.getResources().getDisplayMetrics().density;
            LogUtil.i("scale", scale + "");
        }

        // webview背景色
        String backgroundColor = intent.getStringExtra("backgroundColor");
        LogUtil.i("backgroundColor", backgroundColor);
        if(backgroundColor != null && backgroundColor.length() > 0) {
            int color = Color.parseColor(backgroundColor);
            webView.setBackgroundColor(color);
        }

        // title
        String sTitle = intent.getStringExtra("title");
        String sSubTitle = intent.getStringExtra("subTitle");
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
        String titleColor = intent.getStringExtra("titleColor");
        LogUtil.i("titleColor", titleColor);
        if(titleColor != null && titleColor.length() > 0) {
            int color = Color.parseColor(titleColor);
            LogUtil.i("titleColor ", color + "");
            title.setTextColor(color);
            subTitle.setTextColor(color);
        }

        // titleBgColor
        String titleBgColor = intent.getStringExtra("titleBgColor");
        LogUtil.i("titleBgColor", titleBgColor);
        if(titleBgColor != null && titleBgColor.length() > 0) {
            setTitleBgColor(titleBgColor);
        }

        // 是否隐藏back键
        String hideBackButton = intent.getStringExtra("hideBackButton");
        LogUtil.i("hideBackButton ", hideBackButton);
        if(hideBackButton != null && hideBackButton.equals("true")) {
            back.setVisibility(View.GONE);
        }
        else {
            back.setVisibility(View.VISIBLE);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click back");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.trigger('back');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //
                    }
                });
            }
        });

        // 自定义back图片base64
        String backIcon = intent.getStringExtra("backIcon");
        LogUtil.i("backIcon ", backIcon);
        if(backIcon != null && backIcon.length() > 0) {
            setBackIcon(backIcon);
        }

        optionMenuText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuText");
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('optionMenu');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //
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
                        //
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
                        //
                    }
                });
            }
        });

        // optionMenu文字
        String optionMenu = intent.getStringExtra("optionMenu");
        String optionMenuIcon1 = intent.getStringExtra("optionMenuIcon1");
        String optionMenuIcon2 = intent.getStringExtra("optionMenuIcon2");
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
            optionMenuText.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv1.setVisibility(View.GONE);
        }
        if(optionMenuIcon2 != null && optionMenuIcon2.length() > 0) {
            Bitmap bitmap = ImgUtil.parseBase64(optionMenuIcon2);
            optionMenuIv2.setImageBitmap(bitmap);
            optionMenuText.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv2.setVisibility(View.GONE);
        }

        // 是否读取网页标题
        String readTitle = intent.getStringExtra("readTitle");
        LogUtil.i("readTitle ", readTitle + "");
        this.readTitle = readTitle != null && readTitle.equals("true");

        initPlugins();

        WebSettings webSettings = webView.getSettings();
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " app/ZhuanQuan/" + BuildConfig.VERSION_NAME);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        // 支持缩放viewport
        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
        webView.setDrawingCacheEnabled(true);

        webView.setSwipeRefreshLayout(swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtil.i("swipeRefreshLayout onRefresh");
                // TODO: 极低概率下ZhuanQuanJSBridge还没有加载出来，进入卡死状态
                webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.trigger('refresh');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //
                    }
                });
            }
        });

        MyWebViewClient webViewClient = new MyWebViewClient(this);
        webView.setWebViewClient(webViewClient);
        MyWebChromeClient webChromeClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(webChromeClient);
        webView.setWebContentsDebuggingEnabled(true);

        // 从上个启动活动获取需要加载的url
        url = intent.getStringExtra("__url__");
        LogUtil.i("__url__: " + url);

        // 离线包地址添加cookie
        syncCookie();
        if(url != null && !url.equals("")) {
            webView.loadUrl(url);
        }
        else {
            webView.loadUrl("about:blank");
        }
    }

    public void syncCookie() {
        LogUtil.i("syncCookie");
        CookieSyncManager.createInstance(this);
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

    private void initPlugins() {
        setTitlePlugin = new SetTitlePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE, setTitlePlugin);

        setSubTitlePlugin = new SetSubTitlePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_SUB_TITLE, setSubTitlePlugin);

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

        refreshPlugin = new RefreshPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.REFRESH, refreshPlugin);

        refreshStatePlugin = new RefreshStatePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.REFRESH_STATE, refreshStatePlugin);

        loginWeiboPlugin = new LoginWeiboPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.LOGIN_WEIBO, loginWeiboPlugin);

        getPreferencePlugin = new GetPreferencePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.GET_PRE_FERENCE, getPreferencePlugin);

        setPreferencePlugin = new SetPreferencePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_PRE_FERENCE, setPreferencePlugin);

        setOptionMenuPlugin = new SetOptionMenuPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_OPTIONMENU, setOptionMenuPlugin);

        setTitleBgColorPlugin = new SetTitleBgColorPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE_BG_COLOR, setTitleBgColorPlugin);

        moveTaskToBackPlugin = new MoveTaskToBackPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.MOVE_TASK_TO_BACK, moveTaskToBackPlugin);

        openUriPlugin = new OpenUriPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.OPEN_URI, openUriPlugin);

        setCookiePlugin = new SetCookiePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_COOKIE, setCookiePlugin);

        weiboLoginPlugin = new WeiboLoginPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.WEIBO_LOGIN, weiboLoginPlugin);

        loginOutPlugin = new LoginOutPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.LOGIN_OUT, loginOutPlugin);

        notificationPlugin = new NotifyPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.NOTIFY, notificationPlugin);

        albumPlugin = new AlbumPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.ALBUM, albumPlugin);

        promptPlugin = new PromptPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.PROMPT, promptPlugin);

        downloadPlugin = new DownloadPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.DOWNLOAD, downloadPlugin);

        networkInfoPlugin = new NetworkInfoPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.NETWORK_INFO, networkInfoPlugin);

        loginPlugin = new LoginPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.LOGIN, loginPlugin);

        mediaPlugin = new MediaPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.MEDIA, mediaPlugin);

        setBackPlugin = new SetBackPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_BACK, setBackPlugin);

        setCachePlugin = new SetCachePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SET_CACHE, setCachePlugin);

        getCachePlugin = new GetCachePlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.GET_CACHE, getCachePlugin);
    }

    public void setDefaultTitle(String s) {
        LogUtil.i("setDefaultTitle: " + s + ", " + hasSetTitle);
        if(!hasSetTitle && readTitle) {
            // 有可能没有titleBar
            if(title != null) {
                title.setText(s);
            }
        }
    }
    public void setTitle(String s) {
        LogUtil.i("setTitle: " + s + ", " + hasSetTitle);
        // 有可能没有titleBar
        if(title != null) {
            title.setText(s);
        }
        hasSetTitle = true;
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
    public void pushWindow(String url, JSONObject params) {
        LogUtil.i("pushWindow: " + url + "," + params.toJSONString());
        Intent intent = new Intent(X5Activity.this, X5Activity.class);
        intent.putExtra("__url__", url);
        for(String key : params.keySet()) {
            String value = params.getString(key);
            intent.putExtra(key, value);
        }
        startActivityForResult(intent, PUSH_WINDOW_OK);
    }
    public WebView getWebView() {
        return webView;
    }
    public String getUrl() {
        return url;
    }
    public void hideBackButton() {
        back.setVisibility(View.GONE);
    }
    public void showBackButton() {
        back.setVisibility(View.VISIBLE);
    }
    public void setOptionMenuText(String text, String textColor) {
        if(text != null && text.length() > 0) {
            optionMenuText.setText(text);
            int color = Color.parseColor(textColor);
            LogUtil.i("titleColor ", color + "");
            optionMenuText.setTextColor(color);
            optionMenuText.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuText.setVisibility(View.GONE);
        }
    }
    public void setOptionMenuImg1(String img) {
        if(img != null && img.length() > 0) {
            optionMenuIv1.setImageBitmap(ImgUtil.parseBase64(img));
            optionMenuIv1.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv1.setVisibility(View.GONE);
        }
    }
    public void setOptionMenuImg2(String img) {
        if(img != null && img.length() > 0) {
            optionMenuIv2.setImageBitmap(ImgUtil.parseBase64(img));
            optionMenuIv2.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuIv2.setVisibility(View.GONE);
        }
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
    }
    private void altFullScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    private String getRealPathFromUri(Uri contentUri) {
        String filePath = contentUri.toString();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(contentUri, filePathColumn, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }
    public void media(final String key, final JSONObject value, final String clientId) {
        LogUtil.i("serviceConnection is null:", (serviceConnection == null) + "");
        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    LogUtil.i("onServiceConnected");
                    playBinder = (MediaService.PlayBinder) service;
                    playBinder.start(X5Activity.this);
                    mediaNext(key, value, clientId);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    LogUtil.i("onServiceDisconnected");
                }
            };
            Intent intent = new Intent(this, MediaService.class);
            startService(intent);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
        else {
            mediaNext(key, value, clientId);
        }
    }
    private void mediaNext(String key, JSONObject value, String clientId) {
        if(playBinder != null && key != null) {
            switch(key) {
                case "info":
                    playBinder.info(value, clientId);
                    break;
                case "play":
                    playBinder.play(clientId);
                    break;
                case "pause":
                playBinder.pause(clientId);
                    break;
                case "stop":
                    playBinder.stop(clientId);
                    break;
                case "seek":
                    playBinder.seek(value, clientId);
                    break;
            }
        }
    }
    public void setBackIcon(String img) {
        LogUtil.i("setBackIcon", img);
        Bitmap bitmap = ImgUtil.parseBase64(img);
        back.setImageBitmap(bitmap);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtil.i("keyup: " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            LogUtil.i("KEYCODE_BACK");
            webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.trigger('back');", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            mSsoHandler = null;
        }
        else {
            switch (requestCode) {
                case PUSH_WINDOW_OK:
                    if (resultCode == RESULT_OK) {
                        popWindowParam = data.getStringExtra("param");
                        LogUtil.i("PUSH_WINDOW_OK: " + popWindowParam);
                    }
                    break;
                case REQUEST_ALBUM_OK:
                    if (resultCode == RESULT_OK) {
                        List<Uri> list = Matisse.obtainResult(data);
                        LogUtil.i("Matisse", "mSelected: " + list);
                        if(list.size() == 0) {
                            albumPlugin.cancel();
                            return;
                        }
                        int permissionRead = ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE);
                        int permissionWrite = ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        LogUtil.i("permissionRead", permissionRead + "");
                        LogUtil.i("permissionWrite", permissionWrite + "");
                        if(permissionRead != PackageManager.PERMISSION_GRANTED || permissionWrite != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, 1);
                            albumPlugin.cancel();
                            return;
                        }
                        ArrayList<String> res = new ArrayList<String>();
                        for(Uri uri : list) {
                            // 获取图片高宽并进行压缩
                            String file = getRealPathFromUri(uri);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(file, options);
                            int width = options.outWidth;
                            int height = options.outHeight;
                            String type = options.outMimeType;
                            LogUtil.i("REQUEST_ALBUM_OK: ", width + " " + height + " " + type);
                            int inSampleSize = 1;
                            int maxWidth = 1000;
                            int maxHeight = 1000;
                            int widthRatio = Math.max(1, width / maxWidth);
                            int heightRatio = Math.max(1, height / maxHeight);
                            if(widthRatio < heightRatio) {
                                inSampleSize = widthRatio;
                            }
                            else if(widthRatio > heightRatio) {
                                inSampleSize = heightRatio;
                            }
                            //读取图片
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = inSampleSize;
                            Bitmap bitmap = BitmapFactory.decodeFile(file, options);
                            if(bitmap == null) {
                                LogUtil.i("REQUEST_ALBUM_OK", "null");
                                break;
                            }
                            LogUtil.i("REQUEST_ALBUM_OK", bitmap.getByteCount() + " " + bitmap.getWidth() + " " + bitmap.getHeight());

                            ByteArrayOutputStream baos = null;
                            try {
                                baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                                baos.flush();
                                String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                                base64 = base64.replaceAll("\n", "");
                                res.add(base64);
                            } catch(FileNotFoundException e) {
                                e.printStackTrace();
                            } catch(IOException e) {
                                e.printStackTrace();
                            } finally {
                                bitmap.recycle();
                                try {
                                    if(baos != null) {
                                        baos.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(res.size() > 0) {
                            String[] l = new String[res.size()];
                            res.toArray(l);
                            albumPlugin.success(l);
                        }
                        else {
                            albumPlugin.error();
                        }
                    }
                    else if(resultCode == RESULT_CANCELED) {
                        albumPlugin.cancel();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        LogUtil.i("onStart: ", url);
        super.onStart();
    }
    @Override
    protected void onRestart() {
        LogUtil.i("onRestart: ", url);
        super.onRestart();
        LogUtil.i("resume: ", popWindowParam);
        webView.onResume();
        LogUtil.i("resume: ", popWindowParam);
        webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('resume', " + popWindowParam + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
        popWindowParam = null;
    }
    @Override
    protected void onResume() {
        LogUtil.i("onResume: ", url);
        super.onResume();
        MobclickAgent.onPageStart(url);
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
        LogUtil.i("onPause: ", url);
        super.onPause();
        MobclickAgent.onPageEnd(url);
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onStop() {
        LogUtil.i("onStop: ", url);
        super.onStop();
        webView.onPause();
        webView.evaluateJavascript("window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('pause');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
        if(playBinder != null) {
            playBinder.end(this);
            playBinder = null;
        }
        if(serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }
    @Override
    protected void onDestroy() {
        LogUtil.i("onDestroy: ", url);
        super.onDestroy();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.setSwipeRefreshLayout(null);
        webView.clearHistory();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        webView = null;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void loginWeibo() {
        LogUtil.i("loginWeibo");
        WbSdk.install(this, new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        mSsoHandler = new SsoHandler(this);
        mSsoHandler.authorize(new SelfWbAuthListener());
    }

    private class SelfWbAuthListener implements WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken mAccessToken) {
            LogUtil.i("SelfWbAuthListener onSuccess", mAccessToken.isSessionValid() + "");
            if(mAccessToken.isSessionValid()) {
                final String openId = mAccessToken.getUid();
                final String token = mAccessToken.getToken();
                loginWeiboPlugin.success(openId, token);
            }
        }

        @Override
        public void cancel() {
            LogUtil.i("SelfWbAuthListener cancel");
            loginWeiboPlugin.cancel();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            LogUtil.i("SelfWbAuthListener onFailure", errorMessage.getErrorMessage());
            loginWeiboPlugin.failure(errorMessage.getErrorMessage());
        }
    }
}
