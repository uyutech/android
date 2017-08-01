package net.xiguo.test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.login.UserInfo;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.URLs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        bgi = (ImageView) findViewById(R.id.bgi);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        domain = (TextView) findViewById(R.id.domain);
        copyright = (TextView) findViewById(R.id.copyright);

        // 背景渐显
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(1000);
        bgi.setAnimation(alphaAnimation);
        alphaAnimation.startNow();
        timeStart = new Date().getTime();

        Intent intent = getIntent();
        LogUtil.i("schema: " + intent.getScheme());
        Uri uri = intent.getData();
        if(uri != null) {
            LogUtil.i("schema2: " + uri.getScheme());
            LogUtil.i("host: " + uri.getHost());
            LogUtil.i("port: " + uri.getPort());
            LogUtil.i("path: " + uri.getPath());
            LogUtil.i("query: " + uri.getQuery());
            LogUtil.i("param: " + uri.getQueryParameter("key"));
        }

        checkUpdate();
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
                    Request request = new Request.Builder()
                            .url(URLs.CHECK_H5_PACKAGE_URL)
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
                                checkSession();
                            }
                        });
                        return;
                    }
                    final JSONObject json = JSON.parseObject(responseBody);
                    boolean success = json.getBoolean("success");
                    if(success) {
                        int version = json.getIntValue("version");
                        // 获取本地版本信息
                        SharedPreferences sharedPreferences = getSharedPreferences("h5_package", MODE_PRIVATE);
                        final int curVersion = sharedPreferences.getInt("version", 0);
                        LogUtil.i("checkUpdate version: ", version + ", " + curVersion);
                        if(curVersion < version) {
                            SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences("h5_package", Context.MODE_PRIVATE).edit();
                            editor.putInt("version", version);
                            editor.putBoolean("hasUnZip", false);
                            editor.apply();
                            final String url = json.getString("url");
                            LogUtil.i("Download h5zip: ", url);
                            OkHttpClient client2 = new OkHttpClient
                                    .Builder()
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
                                            checkSession();
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
                                                checkSession();
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
//                                        LogUtil.i("Download h5zip progress: ", sum + ", " + total + ", " + progress + "");
                                        outStream.write(buffer, 0, len);
                                    }
                                    final ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
                                    LogUtil.i("Download h5zip finish： ", outStream.size() + ", " + inputStream.available());
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            unZipH5Pack(inputStream);
                                            checkSession();
                                        }
                                    });
                                }
                            });
                        }
                        else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    unZipH5Pack();
                                    checkSession();
                                }
                            });
                        }
                    }
                    else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unZipH5Pack();
                                checkSession();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.i("checkSession exception", e.toString());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            unZipH5Pack();
                            checkSession();
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
        SharedPreferences sharedPreferences = getSharedPreferences("h5_package", MODE_PRIVATE);
        boolean hasUnZip = sharedPreferences.getBoolean("hasUnZip", false);
        LogUtil.i("unZipH5Pack hasUnZip: " + hasUnZip);
        if(hasUnZip) {
            return;
        }
        // 标识已经解压
        SharedPreferences.Editor editor = this.getSharedPreferences("h5_package", Context.MODE_PRIVATE).edit();
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

    private void checkSession() {
        // 获取已登录信息
        SharedPreferences sharedPreferences = getSharedPreferences("cookie", MODE_PRIVATE);
        final String JSESSIONID = sharedPreferences.getString("JSESSIONID", "");
        final String JSESSIONID_FULL = sharedPreferences.getString("JSESSIONID_FULL", "");
        LogUtil.i("JSESSIONID: ", JSESSIONID);
        LogUtil.i("JSESSIONID_FULL: ", JSESSIONID_FULL);

        // 检测登录
        boolean focusLogin = false;
        if(JSESSIONID.isEmpty() || focusLogin) {
            // 暂停3s后跳转
            showLogin();
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.i("checkSession run");
                    try {
                        OkHttpClient client = new OkHttpClient
                                .Builder()
                                .build();
                        String url = URLs.LOGIN_DOMAIN + URLs.SESSON_CHECK;
                        LogUtil.i(url);
                        Request request = new Request.Builder()
                                .url(url)
                                .header("cookie", "JSESSIONID=" + JSESSIONID)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseBody = response.body().string();
                        LogUtil.i("checkSession: " + responseBody);
                        if(responseBody.isEmpty()) {
                            LogUtil.i("checkSession isEmpty");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showLogin();
                                }
                            });
                            return;
                        }
                        final JSONObject json = JSON.parseObject(responseBody);
                        boolean success = json.getBoolean("success");
                        if(success) {
                            MyCookies.add(JSESSIONID_FULL);
                            // 记录用户信息
                            JSONObject data = json.getJSONObject("data");
                            UserInfo.setUserInfo(data);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject data = json.getJSONObject("data");
                                    int regStat = data.getIntValue("regStat");
                                    Intent intent = new Intent(MainActivity.this, X5Activity.class);
                                    String url;
                                    if(regStat >= 4) {
                                        url = URLs.H5_DOMAIN + "index.html";
                                    }
                                    else {
                                        url = URLs.H5_DOMAIN + "guide.html?step=" + regStat;
                                    }
//                                    url = "http://192.168.100.199:8080/index.html?id=1";
                                    intent.putExtra("url", url);
                                    intent.putExtra("firstWeb", true);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                }
                            });
                        }
                        else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showLogin();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLogin();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void showLogin() {
        long end = new Date().getTime();
        int time;
        if(end - timeStart >= 3000) {
            time = 0;
        }
        else {
            time = 3000 - ((int)(end - timeStart));
        }
        LogUtil.i("showLogin: ", time + "");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }, time);
    }
}
