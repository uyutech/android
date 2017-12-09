package cc.circling;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import cc.circling.login.oauth.Constants;
import cc.circling.plugin.AlbumPlugin;
import cc.circling.plugin.DownloadPlugin;
import cc.circling.plugin.HideOptionMenuPlugin;
import cc.circling.plugin.LoginOutPlugin;
import cc.circling.plugin.LoginWeiboPlugin;
import cc.circling.plugin.GetPreferencePlugin;
import cc.circling.plugin.MoveTaskToBackPlugin;
import cc.circling.plugin.NetworkInfoPlugin;
import cc.circling.plugin.NotificationPlugin;
import cc.circling.plugin.OpenUriPlugin;
import cc.circling.plugin.PromptPlugin;
import cc.circling.plugin.SetOptionMenuPlugin;
import cc.circling.plugin.SetPreferencePlugin;
import cc.circling.plugin.SetCookiePlugin;
import cc.circling.plugin.SetSubTitlePlugin;
import cc.circling.plugin.SetTitleBgColorPlugin;
import cc.circling.plugin.ShowOptionMenuPlugin;
import cc.circling.plugin.RefreshPlugin;
import cc.circling.plugin.RefreshStatePlugin;
import cc.circling.plugin.WeiboLoginPlugin;
import cc.circling.utils.AndroidBug5497Workaround;
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
    private ShowOptionMenuPlugin showOptionMenuPlugin;
    private HideOptionMenuPlugin hideOptionMenuPlugin;
    private SetOptionMenuPlugin setOptionMenuPlugin;
    private SetTitleBgColorPlugin setTitleBgColorPlugin;
    private MoveTaskToBackPlugin moveTaskToBackPlugin;
    private OpenUriPlugin openUriPlugin;
    private SetCookiePlugin setCookiePlugin;
    private WeiboLoginPlugin weiboLoginPlugin;
    private LoginOutPlugin loginOutPlugin;
    private NotificationPlugin notificationPlugin;
    private AlbumPlugin albumPlugin;
    private PromptPlugin promptPlugin;
    private DownloadPlugin downloadPlugin;
    private NetworkInfoPlugin networkInfoPlugin;

    private LinearLayout titleBar;
    private TextView title;
    private TextView subTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView back;
    private WebView webView;
    private TextView optionMenuText;
    private LinearLayout web;
    private FrameLayout fullScreenView;

    private String url;
    private String popWindowParam;
    private boolean firstRun;
    private boolean hasSetTitle = false;
    private boolean readTitle;

    private SsoHandler mSsoHandler;

    private static CookieManager cookieManager;

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

        titleBar = (LinearLayout) findViewById(R.id.titleBar);
        title = (TextView) findViewById(R.id.title);
        subTitle = (TextView) findViewById(R.id.subTitle);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        webView = (WebView) findViewById(R.id.x5);
        back = (ImageView) findViewById(R.id.back);
        optionMenuText = (TextView) findViewById(R.id.optionMenuText);
        web = (LinearLayout) findViewById(R.id.web);
        fullScreenView = (FrameLayout) findViewById(R.id.fullScreen);

        // webview背景色
        String backgroundColor = intent.getStringExtra("backgroundColor");
        LogUtil.i("backgroundColor ", backgroundColor);
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
        }

        // titleBgColor
        String titleBgColor = intent.getStringExtra("titleBgColor");
        LogUtil.i("titleBgColor ", titleBgColor);
        if(titleBgColor != null && titleBgColor.length() > 0) {
            setTitleBgColor(titleBgColor);
        }

        // 是否隐藏back键
        String hideBackButton = intent.getStringExtra("hideBackButton");
        LogUtil.i("hideBackButton ", hideBackButton + "");
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
                webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('back');");
            }
        });

        // 是否显示optionMenu
        String showOptionMenu = intent.getStringExtra("showOptionMenu");
        LogUtil.i("showOptionMenu ", showOptionMenu + "");
        if(showOptionMenu != null && showOptionMenu.equals("")) {
            optionMenuText.setVisibility(View.VISIBLE);
        }
        else {
            optionMenuText.setVisibility(View.GONE);
        }
        optionMenuText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("click optionMenuText");
                webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('optionMenu');");
            }
        });

        // 是否读取网页标题
        String readTitle = intent.getStringExtra("readTitle");
        LogUtil.i("readTitle ", readTitle + "");
        this.readTitle = readTitle != null && readTitle.equals("true");

        initPlugins();
        firstRun = true;

        WebSettings webSettings = webView.getSettings();
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + " app/ZhuanQuan");
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
                webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('refresh');");
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
//        cookieManager.removeAllCookie();
        // 跨域CORS的ajax设置允许cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
//            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
//                @Override
//                public void onReceiveValue(Boolean value) {
//                }
//            });
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

        showOptionMenuPlugin = new ShowOptionMenuPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.SHOW_OPTIONMENU, showOptionMenuPlugin);

        hideOptionMenuPlugin = new HideOptionMenuPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.HIDE_OPTIONMENU, hideOptionMenuPlugin);

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

        notificationPlugin = new NotificationPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.NOTIFY, notificationPlugin);

        albumPlugin = new AlbumPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.ALBUM, albumPlugin);

        promptPlugin = new PromptPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.PROMPT, promptPlugin);

        downloadPlugin = new DownloadPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.DOWNLOAD, downloadPlugin);

        networkInfoPlugin = new NetworkInfoPlugin(this);
        H5EventDispatcher.addEventListener(H5Plugin.NETWORK_INFO, networkInfoPlugin);
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
    public void hideOptionMenu() {
        optionMenuText.setVisibility(View.GONE);
    }
    public void showOptionMenu() {
        optionMenuText.setVisibility(View.VISIBLE);
    }
    public void setOptionMenuText(String text) {
        optionMenuText.setText(text);
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
                        Uri uri = data.getData();
                        LogUtil.i("REQUEST_ALBUM_OK: " + uri.toString());
                        // 获取路径
                        String[] filePathColumns = { MediaStore.Images.Media.DATA };
                        Cursor c = getContentResolver().query(uri, filePathColumns, null, null, null);
                        c.moveToFirst();
                        int columnIndex = c.getColumnIndex(filePathColumns[0]);
                        String file = c.getString(columnIndex);
                        c.close();
                        // 获取图片高宽并进行压缩
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
                        LogUtil.i("REQUEST_ALBUM_OK", bitmap.getByteCount() + " " + bitmap.getWidth() + " " + bitmap.getHeight());

                        ByteArrayOutputStream baos = null;
                        try {
                            baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            baos.flush();
                            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                            albumPlugin.success(base64);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();
                            albumPlugin.error();
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
                    else if(resultCode == RESULT_CANCELED) {
                        albumPlugin.cancel();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("onStart: ", url + ", " + firstRun);
        if(!firstRun) {
            webView.onResume();
            webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('resume', " + popWindowParam + ");");
            popWindowParam = null;
        }
        else {
            firstRun = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(url);
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(url);
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        webView.onPause();
        LogUtil.i("onStop: ", url);
        webView.loadUrl("javascript: ZhuanQuanJSBridge.trigger('pause');");
    }
    @Override
    protected void onDestroy() {
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
