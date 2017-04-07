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
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import net.xiguo.test.login.LoginFragment;
import net.xiguo.test.login.RegisterFragment;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.URLs;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity {
    private TextView loginLabel;
    private TextView registerLabel;
    private boolean isLoginShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        loginLabel = (TextView) findViewById(R.id.loginLabel);
        registerLabel = (TextView) findViewById(R.id.registerLabel);
        loginLabel.setTextColor(ContextCompat.getColor(this, R.color.linkActive));

        final LoginFragment loginFragment = new LoginFragment();
        final RegisterFragment registerFragment = new RegisterFragment();

        loginLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoginShow) {
                    isLoginShow = !isLoginShow;
                    registerLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.link));
                    loginLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.linkActive));
                    replaceFragment(loginFragment);
                }
            }
        });
        registerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoginShow) {
                    isLoginShow = !isLoginShow;
                    loginLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.link));
                    registerLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.linkActive));
                    replaceFragment(registerFragment);
                }
            }
        });

        isLoginShow = true;
        replaceFragment(loginFragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.loginFrame, fragment);
        transaction.commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult: " + requestCode + ", " + resultCode + ", " + data.toString());

        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.loginFrame);
        SsoHandler mSsoHandler = loginFragment.getMSsoHandler();
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
