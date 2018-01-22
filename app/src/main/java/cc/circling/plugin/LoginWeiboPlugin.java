package cc.circling.plugin;

import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/4.
 */

public class LoginWeiboPlugin extends H5Plugin {
    private String clientId;

    public LoginWeiboPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("LoginWeiboPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
        this.activity.loginWeibo();
    }

    public void success(String openID, String token) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("openID", openID);
        json.put("token", token);
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
    public void cancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 0);
        json.put("message", "取消授权");
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
    public void failure(String message) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 1);
        json.put("message", message);
        activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //
            }
        });
    }
}
