package cc.circling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.OkHttpDns;
import cc.circling.web.PreferenceEnum;
import cc.circling.web.URLs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private boolean hasUnZipPack = false;
    private ImageView bgi;
    private ProgressBar progressBar;
    private TextView domain;
    private TextView copyright;
    private long timeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        bgi = findViewById(R.id.bgi);
        progressBar = findViewById(R.id.progressBar);
        domain = findViewById(R.id.domain);
        copyright = findViewById(R.id.copyright);

        // 背景渐显
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(1000);
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
                        final int curVersion = sharedPreferences.getInt("version", 56);
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
        if(end - timeStart >= 1000) {
            time = 0;
        }
        else {
            time = 1000 - ((int)(end - timeStart));
        }
        LogUtil.i("showRedirect: ", time + "");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, X5Activity.class);
                String url = URLs.WEB_DOMAIN + "/index.html";
                intent.putExtra("__url__", url);
                intent.putExtra("transparentTitle", "true");
                intent.putExtra("hideBackButton", "true");
                startActivity(intent);
                MainActivity.this.finish();
            }
        }, time);
    }
}
