package net.xiguo.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import net.xiguo.test.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by army on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        String name = userName.getText().toString();
        LogUtil.i(name + name.length());
        sendLoginRequest();
//        if(name.length() > 0) {
//            sendLoginRequest();
//        }
    }

    private void sendLoginRequest() {
        LogUtil.d("sendLoginRequest");
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("sendLoginRequest run");
                HttpURLConnection connection = null;
                BufferedReader br = null;
                try {
                    URL url = new URL("http://192.168.100.103:3000/login");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Charset", "UTF-8");
                    connection.setRequestProperty("Cache-Control", "no-cache");
                    connection.setUseCaches(false);
                    InputStream is = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    showLoginResponse(connection, sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    private void showLoginResponse(HttpURLConnection connection, final String response) {
        LogUtil.d("showLoginResponse");
        Map<String, List<String>> responseHeaderMap = connection.getHeaderFields();
        int size = responseHeaderMap.size();
        for(int i = 0; i < size; i++) {
            String responseHeaderKey = connection.getHeaderFieldKey(i);
            String responseHeaderValue = connection.getHeaderField(i);
            LogUtil.d(responseHeaderKey + ":" + responseHeaderValue);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("showLoginResponse run");
                LogUtil.i(response);
                Intent intent = new Intent(LoginActivity.this, X5Activity.class);
                startActivity(intent);
            }
        });
    }
}
