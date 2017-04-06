package net.xiguo.test;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import net.xiguo.test.utils.LogUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by army on 2017/3/26.
 */

public class MainActivity extends AppCompatActivity {
    private boolean hasUnZipDefaultPack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if(hasUnZipDefaultPack == false) {
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

        Intent intent2 = new Intent(MainActivity.this, LoginActivity2.class);
        startActivity(intent2);
        finish();
    }

    private void unZipH5Pack() {
        Date start = new Date();
        LogUtil.i("start unZipH5Pack: " + start);
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
            LogUtil.i("end unZipH5Pack: " + end);
        }
    }
}
