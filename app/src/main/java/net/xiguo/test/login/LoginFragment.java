package net.xiguo.test.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.LoginActivity;
import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.URLs;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by army on 2017/3/26.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {
    private EditText userName;
    private EditText userPass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        final Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(userName.getText().length() == 0 || userPass.getText().length() == 0) {
                    button.setEnabled(false);
                }
                else {
                    button.setEnabled(true);
                }
            }
        });
        userPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(userName.getText().length() == 0 || userPass.getText().length() == 0) {
                    button.setEnabled(false);
                }
                else {
                    button.setEnabled(true);
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        String name = userName.getText().toString();
        String pass = userPass.getText().toString();
        LogUtil.i("loginClick: " + name + ", " + pass);
//        sendLoginRequest(name, pass);
        LoginActivity loginActivity = (LoginActivity) getActivity();
        loginActivity.login();
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
                    LoginActivity loginActivity = (LoginActivity) getActivity();
                    loginActivity.login();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
