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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.login.UserInfo;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.URLs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private boolean hasUnZipDefaultPack = false;
    private ImageView bgi;
    private TextView domain;
    private TextView copyright;
    private long checkSessionStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        bgi = (ImageView) findViewById(R.id.bgi);
        domain = (TextView) findViewById(R.id.domain);
        copyright = (TextView) findViewById(R.id.copyright);

        // 背景渐显
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(1000);
        bgi.setAnimation(alphaAnimation);
        alphaAnimation.startNow();

        if(!hasUnZipDefaultPack) {
            hasUnZipDefaultPack = true;
            unZipH5Pack();
        }

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
        SharedPreferences sharedPreferences = getSharedPreferences("cookie", MODE_PRIVATE);
        final String JSESSIONID = sharedPreferences.getString("JSESSIONID", "");
        LogUtil.i("JSESSIONID: ", JSESSIONID);
        if(JSESSIONID.isEmpty()) {
            // 暂停3s后跳转
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
            }, 3000);
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.i("checkSession run");
                    checkSessionStart = new Date().getTime();
                    try {
                        OkHttpClient client = new OkHttpClient
                                .Builder()
                                .cookieJar(new CookieJar() {
                                    @Override
                                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                        LogUtil.i("saveFromResponse: " + url);
                                        for (Cookie cookie : cookies) {
                                            LogUtil.i("cookie: " + cookie.toString());
                                            MyCookies.add(cookie.toString());
                                            if (cookie.name().equals("JSESSIONID")) {
                                                LogUtil.i("cookie: ", cookie.value());
                                                SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences("cookie", Context.MODE_PRIVATE).edit();
                                                editor.putString("JSESSIONID", cookie.value());
                                                editor.apply();
                                            }
                                        }
                                    }

                                    @Override
                                    public List<Cookie> loadForRequest(HttpUrl url) {
                                        return new ArrayList<>();
                                    }
                                })
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
                                    intent.putExtra("url", url);
                                    intent.putExtra("firstWeb", true);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                }
                            });
                        }
                        else {
                            showLogin();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLogin();
                    }
                }
            }).start();
        }
    }

    private void unZipH5Pack() {
        Date start = new Date();
        LogUtil.i("start unZipH5Pack: " + start);
        ZipInputStream zis = null;
        try {
            InputStream is = BaseApplication.getContext().getAssets().open("h5.zip");
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
                        byte[] buffer = new byte[1024];
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

    private void showLogin() {
        long end = new Date().getTime();
        int time;
        if(end - checkSessionStart >= 3000) {
            time = 0;
        }
        else {
            time = 3000 - ((int)(end - checkSessionStart));
        }
        LogUtil.i("showLogin: ", time + "");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                        MainActivity.this.finish();
                    }
                });
            }
        }, time);
    }
}
