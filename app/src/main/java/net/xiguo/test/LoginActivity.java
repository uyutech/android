package net.xiguo.test;

import android.content.Context;
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

import net.xiguo.test.login.LoginFragment;
import net.xiguo.test.login.RegisterFragment;
import net.xiguo.test.utils.LogUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity {
    private TextView loginLabel;
    private TextView registerLabel;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        loginLabel = (TextView) findViewById(R.id.loginLabel);
        registerLabel = (TextView) findViewById(R.id.registerLabel);
        loginLabel.setTextColor(ContextCompat.getColor(this, R.color.linkActive));

        final Fragment loginFragment = new LoginFragment();
        final Fragment registerFragment = new RegisterFragment();

        loginLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLogin) {
                    isLogin = !isLogin;
                    registerLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.link));
                    loginLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.linkActive));
                    replaceFragment(loginFragment);
                }
            }
        });
        registerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLogin) {
                    isLogin = !isLogin;
                    loginLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.link));
                    registerLabel.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.linkActive));
                    replaceFragment(registerFragment);
                }
            }
        });

        isLogin = true;
        replaceFragment(loginFragment);

        unZipH5Pack();
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
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
