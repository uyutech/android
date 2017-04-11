package net.xiguo.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import net.xiguo.test.login.LoginFragment;
import net.xiguo.test.login.RegisterFragment;
import net.xiguo.test.login.oauth.Constants;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.URLs;
import net.xiguo.test.widget.ErrorTipText;

import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity {
    private TextView loginLabel;
    private TextView registerLabel;
    private View loginLabelUnder;
    private View registerLabelUnder;
    private boolean isLoginShow;
    private ErrorTipText errorTip;
    private ImageView loginNiang;

    private ImageView loginWeibo;
    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;

    private Fragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        loginWeibo = (ImageView) findViewById(R.id.loginWeibo);
        initWeibo();

        errorTip = (ErrorTipText) findViewById(R.id.errorTip);
        errorTip.setVisibility(View.INVISIBLE);
        errorTip.showNameAndPassNotMatch();

        loginNiang = (ImageView) findViewById(R.id.loginNiang);
        loginLabel = (TextView) findViewById(R.id.loginLabel);
        registerLabel = (TextView) findViewById(R.id.registerLabel);
        registerLabel.setAlpha(0.4f);
        loginLabelUnder = findViewById(R.id.loginLabelUnder);
        registerLabelUnder = findViewById(R.id.registerLabelUnder);

        hideLoginNiang();

        final LoginFragment loginFragment = new LoginFragment();
        final RegisterFragment registerFragment = new RegisterFragment();

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
//        transaction.add(R.id.loginFrame, fragment);
//        transaction.commit();
        lastFragment = fragment;
    }

    public void login() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, X5Activity.class);
                intent.putExtra("url", URLs.H5_DOMAIN + "index.html");
                intent.putExtra("firstWeb", true);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });
    }

    private void initWeibo() {
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
        loginWeibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(new AuthListener());
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

    class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(final Bundle values) {
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                LogUtil.i(mAccessToken.toString());
                AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
            }
            else {
                String code = values.getString("code");
                LogUtil.i("fail: " + code);
            }
        }
        @Override
        public void onCancel() {
            LogUtil.i("onCancel");
        }
        @Override
        public void onWeiboException(WeiboException e) {
            e.printStackTrace();
            LogUtil.i("onWeiboException");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult: " + requestCode + ", " + resultCode + ", " + data.toString());
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
