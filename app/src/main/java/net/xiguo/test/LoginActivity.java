package net.xiguo.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.URLs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText userName;
    private EditText userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        userPass = (EditText) findViewById(R.id.userPass);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        unZipH5Pack();
    }
    @Override
    public void onClick(View v) {
        String name = userName.getText().toString();
        String pass = userPass.getText().toString();
        LogUtil.i("loginClick: " + name + ", " + pass);
//        Intent intent = new Intent(LoginActivity.this, X5Activity.class);
//        startActivity(intent);
        sendLoginRequest(name, pass);
//        if(name.length() > 0 && pass.length() > 0) {
//            sendLoginRequest();
//        }
    }

    private void sendLoginRequest(final String name, final String pass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            LogUtil.i("sendLoginRequest run");
            try {
                OkHttpClient client = new OkHttpClient
                    .Builder()
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            LogUtil.i("saveFromResponse: " + url);
                            for(Cookie cookie : cookies) {
                                LogUtil.i("cookie: " + cookie.toString());
                                MyCookies.add(cookie.toString());
                            }
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            return new List<Cookie>() {
                                @Override
                                public int size() {
                                    return 0;
                                }

                                @Override
                                public boolean isEmpty() {
                                    return false;
                                }

                                @Override
                                public boolean contains(Object o) {
                                    return false;
                                }

                                @NonNull
                                @Override
                                public Iterator<Cookie> iterator() {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public Object[] toArray() {
                                    return new Object[0];
                                }

                                @NonNull
                                @Override
                                public <T> T[] toArray(@NonNull T[] a) {
                                    return null;
                                }

                                @Override
                                public boolean add(Cookie cookie) {
                                    return false;
                                }

                                @Override
                                public boolean remove(Object o) {
                                    return false;
                                }

                                @Override
                                public boolean containsAll(@NonNull Collection<?> c) {
                                    return false;
                                }

                                @Override
                                public boolean addAll(@NonNull Collection<? extends Cookie> c) {
                                    return false;
                                }

                                @Override
                                public boolean addAll(int index, @NonNull Collection<? extends Cookie> c) {
                                    return false;
                                }

                                @Override
                                public boolean removeAll(@NonNull Collection<?> c) {
                                    return false;
                                }

                                @Override
                                public boolean retainAll(@NonNull Collection<?> c) {
                                    return false;
                                }

                                @Override
                                public void clear() {

                                }

                                @Override
                                public Cookie get(int index) {
                                    return null;
                                }

                                @Override
                                public Cookie set(int index, Cookie element) {
                                    return null;
                                }

                                @Override
                                public void add(int index, Cookie element) {

                                }

                                @Override
                                public Cookie remove(int index) {
                                    return null;
                                }

                                @Override
                                public int indexOf(Object o) {
                                    return 0;
                                }

                                @Override
                                public int lastIndexOf(Object o) {
                                    return 0;
                                }

                                @Override
                                public ListIterator<Cookie> listIterator() {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public ListIterator<Cookie> listIterator(int index) {
                                    return null;
                                }

                                @NonNull
                                @Override
                                public List<Cookie> subList(int fromIndex, int toIndex) {
                                    return null;
                                }
                            };
                        }
                    })
                    .build();
                Request request = new Request.Builder()
                    .url(URLs.LOGIN_DOMAIN + "user/login.htm?mobile=" + name + "&password=" + pass)
                    .build();
                Response response = client.newCall(request).execute();
//                Headers headers = response.headers();
//                for(int i = 0; i < headers.size(); i++) {
//                    LogUtil.i("header: " + headers.name(i) + ", " + headers.value(i));
//                }
                String responseBody = response.body().string();
                LogUtil.i("loginResponse: " + responseBody);
                JSONObject json = JSON.parseObject(responseBody);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, X5Activity.class);
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
//            HttpURLConnection connection = null;
//            BufferedReader br = null;
//            try {
//                URL url = new URL(URLs.LOGIN_DOMAIN + "user/login.htm?mobile=" + name + "&password=" + pass);
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setConnectTimeout(8000);
//                connection.setReadTimeout(8000);
//                connection.setRequestProperty("Charset", "UTF-8");
//                connection.setRequestProperty("Cache-Control", "no-cache");
//                connection.setUseCaches(false);
//                InputStream is = connection.getInputStream();
//                br = new BufferedReader(new InputStreamReader(is));
//                StringBuilder sb = new StringBuilder();
//                String line;
//                while((line = br.readLine()) != null) {
//                    sb.append(line);
//                }
//                showLoginResponse(connection, sb.toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (br != null) {
//                    try {
//                        br.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (connection != null) {
//                    connection.disconnect();
//                }
//            }
            }
        }).start();
    }
    private void showLoginResponse(HttpURLConnection connection, final String response) {
        LogUtil.i("showLoginResponse");
        Map<String, List<String>> responseHeaderMap = connection.getHeaderFields();
        int size = responseHeaderMap.size();
        for(int i = 0; i < size; i++) {
            String responseHeaderKey = connection.getHeaderFieldKey(i);
            String responseHeaderValue = connection.getHeaderField(i);
            LogUtil.i(responseHeaderKey + ":" + responseHeaderValue);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            LogUtil.i("showLoginResponse run");
            LogUtil.i(response);
            Intent intent = new Intent(LoginActivity.this, X5Activity.class);
            startActivity(intent);
            }
        });
    }

    private void unZipH5Pack() {
        Date start = new Date();
        LogUtil.i("start unZipH5Pack:" + start);
        ZipInputStream zis = null;
        try {
            InputStream is = BaseApplication.getContext().getAssets().open("test.zip");
            zis = new ZipInputStream(is);
            ZipEntry next = null;
            String fileName = null;
            while((next = zis.getNextEntry()) != null) {
                fileName = next.getName();
                LogUtil.i("upZipName: " + fileName);
                if(next.isDirectory()) {
                }
                else {
                    FileOutputStream fos = null;
                    try {
                        fos = openFileOutput(fileName, Context.MODE_PRIVATE);
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
            LogUtil.i("end unZipH5Pack:" + end);
        }
    }
}
