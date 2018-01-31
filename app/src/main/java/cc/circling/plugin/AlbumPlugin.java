package cc.circling.plugin;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import cc.circling.R;
import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

import static cc.circling.MainActivity.REQUEST_ALBUM_OK;

/**
 * Created by army8735 on 2017/12/6.
 */

public class AlbumPlugin extends H5Plugin {
    private String clientId;

    public AlbumPlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("AlbumPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
        int num = 1;
        JSONObject p = json.getJSONObject("param");
        if(p != null) {
            int max = p.getIntValue("num");
            if(max > 0) {
                num = max;
            }
        }

        int permissionRead = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        LogUtil.i("permissionRead", permissionRead + "");
        LogUtil.i("permissionWrite", permissionWrite + "");
        if(permissionRead != PackageManager.PERMISSION_GRANTED || permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            cancel();
            return;
        }

        Matisse.from(activity)
                .choose(MimeType.of(MimeType.GIF, MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .maxSelectable(num)
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.album_item_height))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_ALBUM_OK);
    }
    public void success(String[] base64) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("base64", base64);
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json.toJSONString() + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
    public void cancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("cancel", true);
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json.toJSONString() + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
    public void error() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json.toJSONString() + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
}
