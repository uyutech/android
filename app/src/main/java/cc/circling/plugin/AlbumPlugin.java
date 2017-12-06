package cc.circling.plugin;

import android.content.Intent;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/6.
 */

public class AlbumPlugin extends H5Plugin {
    private String clientId;

    public AlbumPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("AlbumPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, X5Activity.REQUEST_ALBUM_OK);
    }
    public void success(String base64) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        base64 = base64.replaceAll("\n", "");
        json.put("base64", base64);
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
    public void cancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("cancel", true);
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
    public void error() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
}
