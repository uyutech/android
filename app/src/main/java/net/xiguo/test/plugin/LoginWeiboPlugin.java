package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/4.
 */

public class LoginWeiboPlugin extends H5Plugin {
    private String clientId;

    public LoginWeiboPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("LoginWeiboPlugin: " + params);
        clientId = param.getString("clientId");
        this.activity.loginWeibo();
    }

    public void success(String openId, String token) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("openId", openId);
        json.put("token", token);
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
    public void cancel() {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 0);
        json.put("message", "取消授权");
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
    public void failure(String message) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("type", 1);
        json.put("message", message);
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
    }
}
