package cc.circling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import cc.circling.event.H5EventDispatcher;
import cc.circling.plugin.AlbumPlugin;
import cc.circling.plugin.AlertPlugin;
import cc.circling.plugin.BackPlugin;
import cc.circling.plugin.ConfirmPlugin;
import cc.circling.plugin.DownloadPlugin;
import cc.circling.plugin.GetCachePlugin;
import cc.circling.plugin.GetPreferencePlugin;
import cc.circling.plugin.H5Plugin;
import cc.circling.plugin.HideBackButtonPlugin;
import cc.circling.plugin.HideLoadingPlugin;
import cc.circling.plugin.LoginOutPlugin;
import cc.circling.plugin.LoginPlugin;
import cc.circling.plugin.LoginWeiboPlugin;
import cc.circling.plugin.MediaPlugin;
import cc.circling.plugin.MoveTaskToBackPlugin;
import cc.circling.plugin.NetworkInfoPlugin;
import cc.circling.plugin.NotifyPlugin;
import cc.circling.plugin.OpenUriPlugin;
import cc.circling.plugin.PopWindowPlugin;
import cc.circling.plugin.PromptPlugin;
import cc.circling.plugin.PushWindowPlugin;
import cc.circling.plugin.RefreshPlugin;
import cc.circling.plugin.RefreshStatePlugin;
import cc.circling.plugin.SetBackPlugin;
import cc.circling.plugin.SetCachePlugin;
import cc.circling.plugin.SetOptionMenuPlugin;
import cc.circling.plugin.SetPreferencePlugin;
import cc.circling.plugin.SetSubTitlePlugin;
import cc.circling.plugin.SetTitleBgColorPlugin;
import cc.circling.plugin.SetTitlePlugin;
import cc.circling.plugin.ShowBackButtonPlugin;
import cc.circling.plugin.ShowLoadingPlugin;
import cc.circling.plugin.ToastPlugin;
import cc.circling.plugin.WeiboLoginPlugin;
import cc.circling.utils.AndroidBug5497Workaround;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.PreferenceEnum;
import cc.circling.web.URLs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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

/**
 * Created by army on 2017/3/26.
 */

public class MainActivity extends AppCompatActivity {
    public static final int PUSH_WINDOW_OK = 8735;
    public static final int REQUEST_ALBUM_OK = 8736;
    public static int WIDTH;

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

    private FrameLayout base;
    private FrameLayout open;
    private ImageView bgi;
    private ProgressBar progressBar;
    private TextView domain;
    private TextView copyright;
    private long timeStart;

    private boolean hasUnZipPack = false;
    private WebFragment reserve;
    private ArrayList<WebFragment> wfList;
    private SsoHandler mSsoHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        wfList = new ArrayList();

        // 背景渐显
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(500);
        bgi.setAnimation(alphaAnimation);
        alphaAnimation.startNow();
        timeStart = new Date().getTime();

        Intent intent = getIntent();
        LogUtil.i("schema: " + intent.getScheme());
        LogUtil.i("BUILD_TYPE: " + BuildConfig.BUILD_TYPE);
        LogUtil.i("WEB_DOMAIN: " + BuildConfig.WEB_DOMAIN);
        LogUtil.i("H5_DOMAIN: " + BuildConfig.H5_DOMAIN);
        LogUtil.i("FOCUS_UNZIP: " + BuildConfig.FOCUS_UNZIP);
        LogUtil.i("ONLINE: " + BuildConfig.ONLINE);

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
                        final int curVersion = sharedPreferences.getInt("version", 49);
                        LogUtil.i("checkUpdate version: ", version + ", " + curVersion);
                        if(curVersion < version) {
                            final SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences(PreferenceEnum.H5PACKAGE.name(), Context.MODE_PRIVATE).edit();
                            editor.putInt("version", version);
                            editor.putBoolean("hasUnZip", false);
                            final String url = json.getString("url");
                            LogUtil.i("Download h5zip: ", url);
                            OkHttpClient client2 = new OkHttpClient
                                    .Builder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
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
                } catch (Exception e) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if(fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
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
        prepare();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("transparentTitle", "true");
                bundle.putString("hideBackButton", "true");
                MainActivity.this.enter(URLs.WEB_DOMAIN + "/index.html", bundle);
                // 移除最初的欢迎界面
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        base.removeView(open);
                    }
                }, 10000);
            }
        }, time);
    }

    private void initPlugins() {
//        setTitlePlugin = new SetTitlePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE, setTitlePlugin);
//
//        setSubTitlePlugin = new SetSubTitlePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_SUB_TITLE, setSubTitlePlugin);
//
//        pushWindowPlugin = new PushWindowPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.PUSH_WINDOW, pushWindowPlugin);
//
//        popWindowPlugin = new PopWindowPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.POP_WINDOW, popWindowPlugin);
//
//        backPlugin = new BackPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.BACK, backPlugin);
//
//        toastPlugin = new ToastPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.TOAST, toastPlugin);
//
//        showLoadingPlugin = new ShowLoadingPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SHOW_LOADING, showLoadingPlugin);
//
//        hideLoadingPlugin = new HideLoadingPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.HIDE_LOADING, hideLoadingPlugin);
//
//        alertPlugin = new AlertPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.ALERT, alertPlugin);
//
//        confirmPlugin = new ConfirmPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.CONFIRM, confirmPlugin);
//
//        hideBackButtonPlugin = new HideBackButtonPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.HIDE_BACKBUTTON, hideBackButtonPlugin);
//
//        showBackButtonPlugin = new ShowBackButtonPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SHOW_BACKBUTTON, showBackButtonPlugin);
//
//        refreshPlugin = new RefreshPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.REFRESH, refreshPlugin);
//
//        refreshStatePlugin = new RefreshStatePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.REFRESH_STATE, refreshStatePlugin);
//
//        loginWeiboPlugin = new LoginWeiboPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.LOGIN_WEIBO, loginWeiboPlugin);
//
//        getPreferencePlugin = new GetPreferencePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.GET_PRE_FERENCE, getPreferencePlugin);
//
//        setPreferencePlugin = new SetPreferencePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_PRE_FERENCE, setPreferencePlugin);
//
//        setOptionMenuPlugin = new SetOptionMenuPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_OPTIONMENU, setOptionMenuPlugin);
//
//        setTitleBgColorPlugin = new SetTitleBgColorPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_TITLE_BG_COLOR, setTitleBgColorPlugin);
//
//        moveTaskToBackPlugin = new MoveTaskToBackPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.MOVE_TASK_TO_BACK, moveTaskToBackPlugin);
//
//        openUriPlugin = new OpenUriPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.OPEN_URI, openUriPlugin);
//
//        weiboLoginPlugin = new WeiboLoginPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.WEIBO_LOGIN, weiboLoginPlugin);
//
//        loginOutPlugin = new LoginOutPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.LOGIN_OUT, loginOutPlugin);
//
//        notificationPlugin = new NotifyPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.NOTIFY, notificationPlugin);
//
//        albumPlugin = new AlbumPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.ALBUM, albumPlugin);
//
//        promptPlugin = new PromptPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.PROMPT, promptPlugin);
//
//        downloadPlugin = new DownloadPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.DOWNLOAD, downloadPlugin);
//
//        networkInfoPlugin = new NetworkInfoPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.NETWORK_INFO, networkInfoPlugin);
//
//        loginPlugin = new LoginPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.LOGIN, loginPlugin);
//
//        mediaPlugin = new MediaPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.MEDIA, mediaPlugin);
//
//        setBackPlugin = new SetBackPlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_BACK, setBackPlugin);
//
//        setCachePlugin = new SetCachePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.SET_CACHE, setCachePlugin);
//
//        getCachePlugin = new GetCachePlugin(this);
//        H5EventDispatcher.addEventListener(H5Plugin.GET_CACHE, getCachePlugin);
    }
    private void prepare() {
        reserve = new WebFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.base, reserve);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    private void enter(String url, Bundle bundle) {
        LogUtil.i("enter", url);
        reserve.enter(url, bundle);
        wfList.add(reserve);
        this.prepare();
    }

    @Override
    protected void onStart() {
        LogUtil.i("onStart: ");
        super.onStart();
    }
    @Override
    protected void onRestart() {
        LogUtil.i("onRestart: ");
        super.onRestart();
    }
    @Override
    protected void onResume() {
        LogUtil.i("onResume: ");
        super.onResume();
    }
    @Override
    protected void onPause() {
        LogUtil.i("onPause: ");
        super.onPause();
    }
    @Override
    protected void onStop() {
        LogUtil.i("onStop: ");
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        LogUtil.i("onDestroy: ");
        super.onDestroy();
    }

    public WebView getWebView() {
        return null;
    }
    public void pushWindow(JSONObject data) {
        final String url = data.getString("url");
        JSONObject params = data.getJSONObject("params");
        final Bundle bundle = new Bundle();
        for(String key : params.keySet()) {
            String value = params.getString(key);
            bundle.putString(key, value);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.enter(url, bundle);
            }
        });
    }
    public void setSubTitle(String title) {}
    public void hideBackButton() {}
    public void showBackButton() {}
    public void loginWeibo() {}
}
