package net.xiguo.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import net.xiguo.test.login.ForgetFragment;
import net.xiguo.test.login.LoginFragment;
import net.xiguo.test.login.RegisterFragment;
import net.xiguo.test.login.UserInfo;
import net.xiguo.test.login.oauth.Constants;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;
import net.xiguo.test.web.URLs;
import net.xiguo.test.widget.ErrorTip;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity {
    private LinearLayout loginDiv;
    private LinearLayout forgetDiv;
    private TextView loginLabel;
    private TextView registerLabel;
    private View loginLabelUnder;
    private View registerLabelUnder;
    private boolean isLoginShow;
    private ErrorTip errorTip;
    private ImageView loginNiang;

    private ImageView loginWeibo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;

    private Fragment lastFragment;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;
    private ForgetFragment forgetFragment;
    private ImageView forgetBack;

    private ImageView temp;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        loginDiv = (LinearLayout) findViewById(R.id.loginDiv);
        forgetDiv = (LinearLayout) findViewById(R.id.forgetDiv);
        loginWeibo = (ImageView) findViewById(R.id.loginWeibo);
        initWeibo();

        errorTip = (ErrorTip) findViewById(R.id.errorTip);

        loginNiang = (ImageView) findViewById(R.id.loginNiang);
        loginLabel = (TextView) findViewById(R.id.loginLabel);
        registerLabel = (TextView) findViewById(R.id.registerLabel);
        registerLabel.setAlpha(0.4f);
        loginLabelUnder = findViewById(R.id.loginLabelUnder);
        registerLabelUnder = findViewById(R.id.registerLabelUnder);

        hideLoginNiang();

        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();
        forgetFragment = new ForgetFragment();
        forgetBack = (ImageView) findViewById(R.id.forgetBack);

        temp = (ImageView) findViewById(R.id.temp);
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        loginLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoginShow) {
                    isLoginShow = true;
                    loginLabel.setAlpha(1);
                    registerLabel.setAlpha(0.4f);
                    loginLabelUnder.setVisibility(View.VISIBLE);
                    registerLabelUnder.setVisibility(View.INVISIBLE);
                    showFragment(loginFragment);
                    hideLoginNiang();
                }
            }
        });
        registerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoginShow) {
                    isLoginShow = false;
                    loginLabel.setAlpha(0.4f);
                    registerLabel.setAlpha(1);
                    loginLabelUnder.setVisibility(View.INVISIBLE);
                    registerLabelUnder.setVisibility(View.VISIBLE);
                    showFragment(registerFragment);
                    showLoginNiang();
                }
            }
        });

        isLoginShow = true;
        showFragment(loginFragment);

        forgetBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.showLoginDiv();
            }
        });
    }

    private void hideLoginNiang() {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillBefore(true);
        animationSet.setFillAfter(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 60);
        translateAnimation.setDuration(500);
        animationSet.addAnimation(translateAnimation);
        loginNiang.startAnimation(animationSet);
    }
    private void showLoginNiang() {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillBefore(true);
        animationSet.setFillAfter(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 60, 0);
        translateAnimation.setDuration(500);
        animationSet.addAnimation(translateAnimation);
        loginNiang.startAnimation(animationSet);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment.isAdded()) {
            if (lastFragment != null) {
                transaction.hide(lastFragment).show(fragment).commit();
            }
            else {
                transaction.show(fragment).commit();
            }
        }
        else {
            if (lastFragment != null) {
                transaction.hide(lastFragment).add(R.id.loginFrame, fragment).commit();
            }
            else {
                transaction.add(R.id.loginFrame, fragment).commit();
            }
        }
        lastFragment = fragment;

        loginFragment.clearDelayShowError();
        registerFragment.clearDelayShowError();
        forgetFragment.clearDelayShowError();
        errorTip.hide();
    }

    public void login() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, X5Activity.class);
                intent.putExtra("url", URLs.H5_DOMAIN + "guide.html");
                intent.putExtra("firstWeb", true);
                startActivityForResult(intent, 1);
                LoginActivity.this.finish();
            }
        });
    }
    public void openUrl(int regStat) {
        Intent intent = new Intent(LoginActivity.this, X5Activity.class);
        String url = null;
        if(regStat >= 4) {
            url = URLs.H5_DOMAIN + "index.html";
        }
        else {
            url = URLs.H5_DOMAIN + "guide.html?step=" + regStat;
        }
        intent.putExtra("url", url);
        intent.putExtra("firstWeb", true);
        startActivity(intent);
        this.finish();
    }

    private void initWeibo() {
        WbSdk.install(this, new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        mSsoHandler = new SsoHandler(this);
        loginWeibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("正在打开微博...");
                progressDialog.show();
                mSsoHandler.authorize(new SelfWbAuthListener());
            }
        });
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (mAccessToken.isSessionValid()) {
            String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                    new java.util.Date(mAccessToken.getExpiresTime()));
            LogUtil.i("token: " + mAccessToken.getToken() + ", " + date);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.i("sendWeiboRequest run");
                    try {
                        OkHttpClient client = new OkHttpClient
                                .Builder()
                                .build();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("access_token", mAccessToken.getToken())
                                .build();
                        Request request = new Request.Builder()
                                .url("https://api.weibo.com/oauth2/get_token_info")
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseBody = response.body().string();
                        LogUtil.i("loginWeiboResponse: " + responseBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else {
            LogUtil.i("no weibo");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    public void showForgetDiv() {
        LogUtil.i("showForgetDiv");
        loginDiv.setVisibility(View.GONE);
        forgetDiv.setVisibility(View.VISIBLE);

        loginFragment.clearDelayShowError();
        registerFragment.clearDelayShowError();

        loginFragment.clearDelayShowError();
        registerFragment.clearDelayShowError();
        forgetFragment.clearDelayShowError();
        errorTip.hide();

        if(!forgetFragment.isAdded()) {
            LogUtil.i("forgetFragment add");
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.forgetFrame, forgetFragment);
            transaction.commit();
        }
    }
    public void showLoginDiv() {
        LogUtil.i("showLoginDiv");
        loginDiv.setVisibility(View.VISIBLE);
        forgetDiv.setVisibility(View.GONE);

        loginFragment.clearDelayShowError();
        registerFragment.clearDelayShowError();
        forgetFragment.clearDelayShowError();
        errorTip.hide();
    }
    public ErrorTip getErrorTip() {
        return errorTip;
    }

    private class SelfWbAuthListener implements WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            progressDialog.dismiss();
            LogUtil.i("SelfWbAuthListener onSuccess");
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccessToken = token;
                    LogUtil.i("mAccessToken", mAccessToken.toString());
                    if (mAccessToken.isSessionValid()) {
                        // 保存 Token 到 SharedPreferences
                        AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
                        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("登录中");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        final String openId = mAccessToken.getUid();
                        final String token = mAccessToken.getToken();
                        final String channelType = "1";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.i("LOGIN_BY_OPEN_ID run");
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
                                                        if(cookie.name().equals("JSESSIONID")) {
                                                            LogUtil.i("cookie: ", cookie.value());
                                                            SharedPreferences.Editor editor = LoginActivity.this.getSharedPreferences("cookie", Context.MODE_PRIVATE).edit();
                                                            editor.putString("JSESSIONID", cookie.value());
                                                            editor.putString("JSESSIONID_FULL", cookie.value());
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
                                    String url = URLs.LOGIN_DOMAIN + URLs.LOGIN_BY_OPEN_ID
                                            + "?openId=" + android.net.Uri.encode(openId)
                                            + "&token=" + android.net.Uri.encode(token)
                                            + "&channelType=" + android.net.Uri.encode(channelType);
                                    LogUtil.i(url);
                                    Request request = new Request.Builder()
                                            .url(url)
                                            .build();
                                    Response response = client.newCall(request).execute();
                                    String responseBody = response.body().string();
                                    LogUtil.i("LOGIN_BY_OPEN_ID: " + responseBody);
                                    if(responseBody.isEmpty()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.hide();
                                                Toast toast = Toast.makeText(LoginActivity.this, "网络异常请重试", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
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
                                                progressDialog.hide();
                                                progressDialog.dismiss();
                                                Toast toast = Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                JSONObject data = json.getJSONObject("data");
                                                LoginActivity.this.openUrl(data.getIntValue("regStat"));
                                            }
                                        });
                                    }
                                    else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.hide();
                                                String message = json.getString("message");
                                                if(message == null || message.isEmpty()) {
                                                    message = "网络异常请重试";
                                                }
                                                Toast toast = Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.hide();
                                            Toast toast = Toast.makeText(LoginActivity.this, "网络异常请重试", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }
            });
        }

        @Override
        public void cancel() {
            progressDialog.dismiss();
            LogUtil.i("SelfWbAuthListener cancel");
            Toast toast = Toast.makeText(LoginActivity.this, "取消授权", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            progressDialog.dismiss();
            LogUtil.i("SelfWbAuthListener onFailure", errorMessage.getErrorMessage());
            Toast toast = Toast.makeText(LoginActivity.this, errorMessage.getErrorMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
