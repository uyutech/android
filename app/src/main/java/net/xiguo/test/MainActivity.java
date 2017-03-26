package net.xiguo.test;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import net.xiguo.test.utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

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

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}
