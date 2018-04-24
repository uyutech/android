package cc.circling;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.MultiImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoSourceObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import cc.circling.login.oauth.Constants;
import cc.circling.utils.AndroidBug5497Workaround;
import cc.circling.utils.LogUtil;
import cc.circling.utils.QueryParser;
import cc.circling.web.MyCookies;
import cc.circling.web.OkHttpDns;
import cc.circling.web.PreferenceEnum;
import cc.circling.web.URLs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cc.circling.web.WebView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by army on 2017/3/26.
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, WbShareCallback {
    private static final int RC_INIT = 8734;
    private static final int RC_DOWNLOAD = 8735;
    private static final int RC_ALBUM = 8736;
    public static final int REQUEST_ALBUM_OK = 8737;
    private static final int RC_ALBUM_OLD = 8738;
    public static final int REQUEST_ALBUM_OK_OLD = 8739;
    public static int WIDTH;

    private static String[] umengPerms = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
    };
    private static String[] filePerms = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private String downloadName;
    private String downloadUrl;
    private static int downloadId = 0;

    private static int albumNum = 1;
    private static int albumNumOld = 1;

    private static int notifyId = 0;

    public static ProgressDialog lastProgressDialog;

    private FrameLayout base;
    private FrameLayout open;
    private ImageView bgi;
    private ProgressBar progressBar;
    private TextView domain;
    private TextView copyright;

    private long timeStart;
    private boolean hasUnZipPack = false;
    private WebFragment current;
    private WebFragment reserve;
    private ArrayList<WebFragment> wfList;

    private MediaService.PlayBinder playBinder;
    private ServiceConnection serviceConnection;
    private SsoHandler mSsoHandler;
    private WbShareHandler wbShareHandler;
    private static CookieManager cookieManager;
    private Uri callUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.i("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidBug5497Workaround.assistActivity(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // 获取屏幕宽度
        WindowManager manager = this.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        WIDTH = dm.widthPixels;

        base = findViewById(R.id.base);
        open = findViewById(R.id.open);
        bgi = findViewById(R.id.bgi);
        progressBar = findViewById(R.id.progressBar);
        domain = findViewById(R.id.domain);
        copyright = findViewById(R.id.copyright);
        wfList = new ArrayList<>();

        CookieSyncManager.createInstance(this);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        prepare();
        current = reserve;

        // 背景渐显
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(500);
        bgi.startAnimation(alphaAnimation);
        timeStart = new Date().getTime();

        Intent intent = getIntent();
        callUri = intent.getData();
        if(callUri != null) {
            LogUtil.i("callUri: " + callUri.toString());
        }
        LogUtil.d("BUILD_TYPE: " + BuildConfig.BUILD_TYPE);
        LogUtil.d("WEB_DOMAIN: " + BuildConfig.WEB_DOMAIN);
        LogUtil.d("H5_DOMAIN: " + BuildConfig.H5_DOMAIN);
        LogUtil.d("FOCUS_UNZIP: " + BuildConfig.FOCUS_UNZIP);
        LogUtil.d("ONLINE: " + BuildConfig.ONLINE);

        if(BuildConfig.FOCUS_UNZIP) {
            hasUnZipPack = false;
            SharedPreferences.Editor editor = this.getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), Context.MODE_PRIVATE).edit();
            editor.putBoolean("hasUnZip", false);
            editor.apply();
        }
        if(BuildConfig.ONLINE) {
            showRedirect();
        }
        else {
            checkUpdate();
        }

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.i("onServiceConnected");
                playBinder = (MediaService.PlayBinder) service;
                playBinder.start(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.i("onServiceDisconnected");
            }
        };
        Intent media = new Intent(this, MediaService.class);
        bindService(media, serviceConnection, BIND_AUTO_CREATE);

        WbSdk.install(this, new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));

        if(!EasyPermissions.hasPermissions(this, umengPerms)) {
            EasyPermissions.requestPermissions(this, "为了便于智能统计分析和推送，申请读取手机型号基本信息。",
                    RC_INIT, umengPerms);
        }
    }
    @Override
    protected void onRestart() {
        LogUtil.i("onRestart: ");
        super.onRestart();
        current.resume();
    }
    @Override
    protected void onResume() {
        LogUtil.i("onResume: ");
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
        LogUtil.i("onPause: ");
        super.onPause();
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onStop() {
        LogUtil.i("onStop: ");
        super.onStop();
        current.pause();
    }
    @Override
    protected void onDestroy() {
        LogUtil.i("onDestroy: ");
        super.onDestroy();
        if(serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    private void checkUpdate() {
        // 检测更新情况
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.i("checkUpdate run");
                try {
                    OkHttpClient client = new OkHttpClient
                            .Builder()
                            .dns(OkHttpDns.getInstance())
                            .build();
                    RequestBody requestBody = new FormBody.Builder().build();
                    Request request = new Request.Builder()
                            .url(URLs.H5_DOMAIN + "/h5/version")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    LogUtil.i("checkUpdate: " + responseBody);
                    if(responseBody.isEmpty()) {
                        LogUtil.i("checkUpdate isEmpty");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unZipH5Pack();
                                showRedirect();
                            }
                        });
                        return;
                    }
                    final JSONObject json = JSON.parseObject(responseBody);
                    boolean success = json.getBoolean("success");
                    if(success) {
                        // 远程h5版本
                        int version = json.getIntValue("version");
                        // 更新需要最小android版本
                        int minSdk = json.getIntValue("minSdk");
                        if(BuildConfig.VERSION_CODE < minSdk) {
                            unZipH5Pack();
                            showRedirect();
                        }
                        // 获取本地h5版本信息
                        SharedPreferences sharedPreferences = getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), MODE_PRIVATE);
                        final int curVersion = sharedPreferences.getInt("version", 95);
                        LogUtil.i("checkUpdate version: ", version + ", " + curVersion);
                        if(curVersion < version) {
                            final SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), Context.MODE_PRIVATE).edit();
                            editor.putInt("version", version);
                            editor.putBoolean("hasUnZip", false);
                            final String url = json.getString("url");
                            LogUtil.i("Download h5zip: ", url);
                            OkHttpClient client2 = new OkHttpClient
                                    .Builder()
                                    .dns(OkHttpDns.getInstance())
                                    .connectTimeout(20, TimeUnit.SECONDS)
                                    .readTimeout(300, TimeUnit.SECONDS)
                                    .build();
                            Request request2 = new Request.Builder()
                                    .url(url)
                                    .build();
                            client2.newCall(request2).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    LogUtil.i("Download h5zip failure: ", e.toString());
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            unZipH5Pack();
                                            showRedirect();
                                        }
                                    });
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if(response.code() != 200 && response.code() != 304) {
                                        LogUtil.i("Download h5zip failure: ", response.toString());
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                unZipH5Pack();
                                                showRedirect();
                                            }
                                        });
                                        return;
                                    }
                                    InputStream is = null;
                                    byte[] buffer = new byte[10240];
                                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                                    is = response.body().byteStream();
                                    long total = response.body().contentLength();
                                    LogUtil.i("Download h5zip total: ", total + "");
                                    long sum = 0;
                                    int len;
                                    while((len = is.read(buffer)) != -1) {
                                        sum += len;
                                        int progress = (int) (sum * 1.0f / total * 100);
                                        progressBar.setProgress(progress);
                                        outStream.write(buffer, 0, len);
                                    }
                                    final ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
                                    LogUtil.i("Download h5zip finish： ", outStream.size() + ", " + inputStream.available());
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            unZipH5Pack(inputStream);
                                            showRedirect();
                                        }
                                    });
                                    editor.apply();
                                }
                            });
                        }
                        else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    unZipH5Pack();
                                    showRedirect();
                                }
                            });
                        }
                    }
                    else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unZipH5Pack();
                                showRedirect();
                            }
                        });
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    LogUtil.i("checkUpdate exception", e.toString());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            unZipH5Pack();
                            showRedirect();
                        }
                    });
                }
            }
        }).start();
    }
    private void unZipH5Pack() {
        unZipH5Pack(null);
    }
    private void unZipH5Pack(InputStream is) {
        progressBar.setProgress(100);
        LogUtil.i("unZipH5Pack inputStream is null: " + (is == null));
        // 确保解压方法此次实例只运行一次
        if(hasUnZipPack) {
            return;
        }
        hasUnZipPack = true;

        // 是否已经解压过
        SharedPreferences sharedPreferences = getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), MODE_PRIVATE);
        boolean hasUnZip = sharedPreferences.getBoolean("hasUnZip", false);
        LogUtil.i("unZipH5Pack hasUnZip: " + hasUnZip);
        if(hasUnZip) {
            return;
        }
        // 标识已经解压
        SharedPreferences.Editor editor = this.getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), Context.MODE_PRIVATE).edit();
        editor.putBoolean("hasUnZip", true);
        editor.apply();

        Date start = new Date();
        LogUtil.i("start unZipH5Pack: " + start);
        ZipInputStream zis = null;
        try {
            // 默认读取附带的assets文件夹下的文件
            if(is == null) {
                is = BaseApplication.getContext().getAssets().open("h5.zip");
            }
            zis = new ZipInputStream(is);
            ZipEntry next = null;
            String fileName = null;
            while((next = zis.getNextEntry()) != null) {
                fileName = next.getName();
                String noSepFileName = fileName.replaceAll("/", "__");
                LogUtil.i("upZipName: " + fileName + ", " + noSepFileName + ", isDirectory: " + next.isDirectory());
                if(!next.isDirectory()) {
                    FileOutputStream fos = null;
                    try {
                        fos = openFileOutput(noSepFileName, Context.MODE_PRIVATE);
                        int len;
                        byte[] buffer = new byte[2048];
                        while((len = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if(fos != null) {
                                fos.close();
                            }
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if(zis != null) {
                try {
                    zis.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            Date end = new Date();
            LogUtil.i("end unZipH5Pack: " + end);
        }
    }

    private void showRedirect() {
        progressBar.setProgress(100);
        // 获取已登录信息
        SharedPreferences sharedPreferences = getSharedPreferences(PreferenceEnum.SESSION.name(), MODE_PRIVATE);
        Map<String, ?> map = sharedPreferences.getAll();
        for(String key : map.keySet()) {
            String cookie = map.get(key).toString();
            MyCookies.add(key, cookie);
            LogUtil.i(key, cookie);
        }

        long end = new Date().getTime();
        int time;
        if(end - timeStart >= 2000) {
            time = 0;
        }
        else {
            time = 2000 - ((int)(end - timeStart));
        }
        LogUtil.i("showRedirect: ", time + "");

        // 第一个webview特殊提前加载
        Bundle bundle = new Bundle();
        bundle.putString("transparentTitle", "true");
        bundle.putString("hideBackButton", "true");
        current.load(URLs.WEB_DOMAIN + "/index.html", bundle);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                current.enterOnly();
                wfList.add(current);
                prepare();
                // 欢迎界面移动
                TranslateAnimation translateAnimation = new TranslateAnimation(0, -50,0,0);
                translateAnimation.setDuration(300);
                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        base.removeView(open);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                open.startAnimation(translateAnimation);
                // 然后移除最初的欢迎界面
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(callUri != null) {
                            String host = callUri.getHost();
                            LogUtil.d("host", host);
                            if(host.equalsIgnoreCase("h5")) {
                                String path = callUri.getPath();
                                LogUtil.d("path", path == null ? "" : path);
                                if(path == null || !path.matches("^/\\w+\\.html")) {
                                    return;
                                }
                                String query = callUri.getQuery();
                                LogUtil.d("query", query);
                                String params = callUri.getQueryParameter("params");
                                String search = callUri.getQueryParameter("search");
                                LogUtil.d("params", params == null ? "" : params);
                                LogUtil.d("search", search == null ? "" : search);
                                Bundle bundle = new Bundle();
                                if(params != null && !params.isEmpty()) {
                                    HashMap<String, String> hashMap = QueryParser.parse(params);
                                    for(String key : hashMap.keySet()) {
                                        bundle.putString(key, hashMap.get(key));
                                    }
                                }
                                if(search == null) {
                                    search = "";
                                }
                                enter(URLs.WEB_DOMAIN + path + "?" + search, bundle);
                            }
                            callUri = null;
                        }
                    }
                }, 1000);
            }
        }, time);
    }

    private void prepare() {
        LogUtil.i("prepare");
        reserve = new WebFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.base, reserve);
        fragmentTransaction.commitAllowingStateLoss();
    }
    private void enter(String url, Bundle bundle) {
        LogUtil.d("enter", url + bundle.toString());
        current = reserve;
        current.enter(url, bundle);
        wfList.add(current);
        prepare();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtil.i("onKeyUp: " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            int i = wfList.size();
            LogUtil.i("KEYCODE_BACK", i + "");
            if(i > 0) {
                wfList.get(i - 1).back();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        if(mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            mSsoHandler = null;
        }
        else {
            switch(requestCode) {
                case REQUEST_ALBUM_OK:
                    if(resultCode == RESULT_OK) {
                        List<Uri> list = Matisse.obtainResult(data);
                        LogUtil.i("REQUEST_ALBUM_OK", list.toString());
                        if(list.size() > 0) {
                            current.albumOk(list);
                        }
                    }
                    else {
                        current.albumOk(null);
                    }
                    break;
                case REQUEST_ALBUM_OK_OLD:
                    if(resultCode == RESULT_OK) {
                        List<Uri> list = Matisse.obtainResult(data);
                        LogUtil.i("REQUEST_ALBUM_OK_OLD", list.toString());
                        if(list.size() > 0) {
                            current.albumOkOld(list);
                        }
                    }
                    break;
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        switch(requestCode) {
            case RC_DOWNLOAD:
                download();
                break;
            case RC_ALBUM:
                album();
                break;
            case RC_ALBUM_OLD:
                albumOld();
                break;
        }
    }
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast toast = Toast.makeText(this, "申请权限失败", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    @Override
    public void onWbShareSuccess() {
        LogUtil.i("onWbShareSuccess");
        current.onWbShareSuccess();
    }
    @Override
    public void onWbShareFail() {
        LogUtil.i("onWbShareFail");
        current.onWbShareFail();
    }
    @Override
    public void onWbShareCancel() {
        LogUtil.i("onWbShareCancel");
        current.onWbShareCancel();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("onNewIntent");
        super.onNewIntent(intent);
        if(wbShareHandler != null) {
            wbShareHandler.doResultIntent(intent, this);
        }
        Uri callUri = intent.getData();
        String extraMap = intent.getStringExtra("extraMap");
        LogUtil.d("extraMap", extraMap);
        if(callUri != null) {
            LogUtil.i("callUri: " + callUri.toString());
            String host = callUri.getHost();
            LogUtil.d("host", host);
            if(host.equalsIgnoreCase("h5")) {
                String path = callUri.getPath();
                LogUtil.d("path", path);
                if(!path.matches("^/\\w+\\.html")) {
                    return;
                }
                String query = callUri.getQuery();
                LogUtil.d("query", query);
                String params = callUri.getQueryParameter("params");
                String search = callUri.getQueryParameter("search");
                LogUtil.d("params", params == null ? "" : params);
                LogUtil.d("search", search == null ? "" : search);
                Bundle bundle = new Bundle();
                if(params != null && !params.isEmpty()) {
                    HashMap<String, String> hashMap = QueryParser.parse(params);
                    for(String key : hashMap.keySet()) {
                        bundle.putString(key, hashMap.get(key));
                    }
                }
                if(search == null) {
                    search = "";
                }
                enter(URLs.WEB_DOMAIN + path + "?" + search, bundle);
            }
        }
        else if(extraMap != null && !extraMap.isEmpty()) {
            JSONObject json = JSONObject.parseObject(extraMap);
            LogUtil.d("extraMap", json.toJSONString());
            String path = json.getString("path");
            String params = json.getString("params");
            String search = json.getString("search");
            LogUtil.d("path", path == null ? "" : path);
            LogUtil.d("params", params == null ? "" : params);
            LogUtil.d("search", search == null ? "" : search);
            if(path == null || !path.matches("^/\\w+\\.html")) {
                return;
            }
            Bundle bundle = new Bundle();
            if(params != null && !params.isEmpty()) {
                HashMap<String, String> hashMap = QueryParser.parse(params);
                for(String key : hashMap.keySet()) {
                    bundle.putString(key, hashMap.get(key));
                }
            }
            if(search == null) {
                search = "";
            }
            enter(URLs.WEB_DOMAIN + path + "?" + search, bundle);
        }
    }

    public void pushWindow(JSONObject data) {
        final String url = data.getString("url");
        LogUtil.i("pushWindow", url);
        JSONObject params = data.getJSONObject("params");
        final Bundle bundle = new Bundle();
        for(String key : params.keySet()) {
            String value = params.getString(key);
            bundle.putString(key, value);
        }
        this.enter(url, bundle);
    }
    public void loginWeibo() {
        WbSdk.install(this, new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        mSsoHandler = new SsoHandler(this);
        mSsoHandler.authorize(new SelfWbAuthListener());
    }
    public void back() {
        int i = wfList.size();
        if(i > 1) {
            WebFragment top = wfList.remove(i - 1);
            top.remove();
            current = wfList.get(i - 2);
            current.show();
        }
        else {
            this.moveTaskToBack(true);
        }
    }
    public void remove(WebFragment top) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(top);
        fragmentTransaction.commitAllowingStateLoss();
    }
    public void alert(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
            }
        });
        dialog.show();
    }
    public void confirm(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                current.confirm(true);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                current.confirm(false);
            }
        });
        dialog.show();
    }
    public void download(String url, String name) {
        this.downloadName = name;
        this.downloadUrl = url;
        if(EasyPermissions.hasPermissions(this, filePerms)) {
            download();
        }
        else {
            EasyPermissions.requestPermissions(this, "下载需要读写sd卡权限",
                    RC_DOWNLOAD, filePerms);
        }
    }
    private void download() {
        final String type;
        if(downloadUrl.endsWith(".mp3")) {
            type = "audio/*";
        }
        else if(downloadUrl.endsWith(".mp4")) {
            type = "video/*";
        }
        else {
            type = "image/*";
        }
        // 创建目录
        String directoryPath = "";
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            LogUtil.i("DownloadPlugin: MEDIA_MOUNTED");
            if(downloadUrl.endsWith(".mp3")) {
                directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            }
            else if(downloadUrl.endsWith(".mp4")) {
                directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            }
            else {
                directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            }
        }
        else {
            LogUtil.i("DownloadPlugin: !MEDIA_MOUNTED");
            directoryPath = BaseApplication.getContext().getFilesDir().getAbsolutePath();
        }
        directoryPath += File.separator + "circling";
        File file = new File(directoryPath);
        if(!file.exists()) {
            file.mkdirs();
        }
        LogUtil.i("directoryPath: " + directoryPath);
        FileDownloadHelper.holdContext(BaseApplication.getContext());
        final String path = directoryPath + File.separator + downloadName;
        LogUtil.i("path: " + path);
        File downloadFile = new File(path);
        final int currentID = downloadId++;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "download");
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker("准备下载 " + downloadName);
        builder.setContentTitle("准备下载 " + downloadName);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(BaseApplication.getContext(),
                    BuildConfig.APPLICATION_ID + ".download", downloadFile);
            LogUtil.i("uri: " + uri);
            intent.setDataAndType(uri, type);
        }
        else {
            uri = Uri.fromFile(downloadFile);
            LogUtil.i("uri2: " + uri);
            intent.setDataAndType(uri, type);
        }
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = builder.build();
        final NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(currentID, notification);

        FileDownloader.getImpl().create(downloadUrl)
                .setPath(path)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        LogUtil.i("pending", soFarBytes + ", " + totalBytes);
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        builder.setTicker("开始下载 " + downloadName);
                        builder.setContentTitle("开始下载 " + downloadName);
                        builder.setContentText("进度 0%");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress = (int) (soFarBytes * 1.0f / totalBytes * 100);
                        builder.setTicker("正在下载 " + downloadName);
                        builder.setContentTitle("正在下载 " + downloadName);
                        builder.setContentText("进度 " + progress + "%");
                        builder.setProgress(totalBytes, soFarBytes, false);
                        notificationManager.notify(currentID, builder.build());
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        builder.setTicker("下载完成 " + downloadName);
                        builder.setContentTitle("下载完成 " + downloadName);
                        builder.setContentText("下载完成");
                        builder.setProgress(0, 0, false);
                        notificationManager.notify(currentID, builder.build());
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        LogUtil.i("retry");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        builder.setTicker("下载完成 " + downloadName);
                        builder.setContentTitle("下载完成 " + downloadName);
                        builder.setContentText("下载完成");
                        builder.setProgress(0, 0, false);
                        builder.setContentIntent(pIntent);
                        notificationManager.notify(currentID, builder.build());
                        // 通知媒体库更新，可以在相册等看到
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(uri);
                        LogUtil.i("completed: ", intent.toString());
                        MainActivity.this.sendBroadcast(intent);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        builder.setTicker("下载暂停 " + downloadName);
                        builder.setContentTitle("下载完成 " + downloadName);
                        builder.setContentText("下载暂停");
                        builder.setProgress(totalBytes, soFarBytes, false);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        builder.setTicker("下载错误 " + downloadName);
                        builder.setContentTitle("下载错误 " + downloadName);
                        builder.setContentText("下载错误");
                        builder.setProgress(1, 0, false);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        LogUtil.i("warn");
                    }
                }).start();
    }
    public void showLoading(String title, String message, boolean cancelable) {
        lastProgressDialog = new ProgressDialog(this);
        lastProgressDialog.setTitle(title);
        lastProgressDialog.setMessage(message);
        lastProgressDialog.setCancelable(cancelable);
        lastProgressDialog.setCanceledOnTouchOutside(false);
        lastProgressDialog.show();
    }
    public void hideLoading() {
        if(lastProgressDialog != null) {
            lastProgressDialog.dismiss();
        }
    }
    public void notify(String ticker, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notify");
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        if(content != null) {
            builder.setContentText(content);
        }
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));

        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE);
        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId++, notification);
    }
    public void prompt(String title, String message, String value) {
        EditText et = new EditText(this);
        et.setText(value);
        et.setSelection(value.length());
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setView(et);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String s = et.getText().toString();
                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("value", s);
                current.prompt(json);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                current.prompt(json);
            }
        });
        dialog.show();
    }
    public void popWindow(JSONObject data) {
        this.back();
        current.popWindow(data);
    }
    public void media(String clientId, String key, JSONObject value) {
        LogUtil.i("media", key);
        if(playBinder != null && key != null) {
            switch(key) {
                case "info":
                    playBinder.info(value);
                    break;
                case "play":
                    playBinder.play(value, clientId);
                    break;
                case "pause":
                    playBinder.pause(clientId);
                    break;
                case "stop":
                    playBinder.stop(clientId);
                    break;
                case "release":
                    playBinder.release(clientId);
                    break;
                case "seek":
                    playBinder.seek(value, clientId);
                    break;
            }
        }
    }
    public void evaluateJavascript(String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                current.evaluateJavascript(value);
            }
        });
    }
    public void album(int num) {
        albumNum = num;
        if(EasyPermissions.hasPermissions(this, filePerms)) {
            album();
        }
        else {
            EasyPermissions.requestPermissions(this, "打开相册需要读写sd卡权限",
                    RC_ALBUM, filePerms);
        }
    }
    private void album() {
        Matisse.from(this)
                .choose(MimeType.of(MimeType.GIF, MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .maxSelectable(albumNum)
                .gridExpectedSize(320)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_ALBUM_OK);
    }
    public void albumOld(int num) {
        albumNumOld = num;
        if(EasyPermissions.hasPermissions(this, filePerms)) {
            albumOld();
        }
        else {
            EasyPermissions.requestPermissions(this, "打开相册需要读写sd卡权限",
                    RC_ALBUM_OLD, filePerms);
        }
    }
    private void albumOld() {
        Matisse.from(this)
                .choose(MimeType.of(MimeType.GIF, MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .maxSelectable(albumNumOld)
                .gridExpectedSize(240)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_ALBUM_OK_OLD);
    }
    public void syncCookie(WebView webView) {
        LogUtil.i("syncCookie WebView");
        cookieManager.removeExpiredCookie();
        // 5.0跨域CORS的ajax设置允许cookie
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.d("setAcceptThirdPartyCookies");
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        HashMap<String, String> hashMap = MyCookies.getAll();
        LogUtil.d("cookies: ", hashMap.size() + "");
        for(String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            LogUtil.d("cookie: ", key + ", " + value);
            cookieManager.setCookie(URLs.WEB_DOMAIN, value);
            cookieManager.setCookie(URLs.H5_DOMAIN, value);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.d("flush");
            cookieManager.flush();
        }
        else {
            LogUtil.d("sync");
            CookieSyncManager.getInstance().sync();
        }
    }
    public void syncCookie() {
        LogUtil.i("syncCookie");
        for(WebFragment wf : wfList) {
            syncCookie(wf.getWebView());
        }
    }
    public void shareWb(JSONObject data) {
        LogUtil.i("shareWb");
        if(wbShareHandler == null) {
            wbShareHandler = new WbShareHandler(this);
            wbShareHandler.registerApp();
        }
        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
        String text = data.getString("text");
        if(text != null && !text.isEmpty()) {
            TextObject textObject = new TextObject();
            textObject.text = text;
            weiboMultiMessage.textObject = textObject;
        }
        JSONArray imgList = data.getJSONArray("imgList");
        if(imgList != null && !imgList.isEmpty()) {
            MultiImageObject multiImageObject = new MultiImageObject();
            ArrayList<Uri> imageList = new ArrayList<>();
            for(int i = 0; i < imgList.size(); i++) {
                String s = imgList.get(i).toString();
                Uri uri = Uri.parse(s);
                LogUtil.d("uri", uri.toString());
                imageList.add(uri);
            }
            multiImageObject.imageList = imageList;
            weiboMultiMessage.multiImageObject = multiImageObject;
        }
        String av = data.getString("av");
        if(av != null && !av.isEmpty()) {
            VideoSourceObject videoSourceObject = new VideoSourceObject();
            videoSourceObject.videoPath = Uri.parse(av);
            weiboMultiMessage.videoSourceObject = videoSourceObject;
        }
        wbShareHandler.shareMessage(weiboMultiMessage, false);
    }

    private class SelfWbAuthListener implements WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken mAccessToken) {
            LogUtil.i("SelfWbAuthListener onSuccess", mAccessToken.isSessionValid() + "");
            if(mAccessToken.isSessionValid()) {
                String openId = mAccessToken.getUid();
                String token = mAccessToken.getToken();
                current.loginWeiboSuccess(openId, token);
            }
        }

        @Override
        public void cancel() {
            LogUtil.i("SelfWbAuthListener cancel");
            current.loginWeiboCancel();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            LogUtil.i("SelfWbAuthListener onFailure", errorMessage.getErrorMessage());
            current.loginWeiboError(errorMessage.getErrorMessage());
        }
    }
}
