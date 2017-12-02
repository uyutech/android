package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2017/12/2.
 */

public class LoginOutPlugin extends H5Plugin {
    private String clientId;

    public LoginOutPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("WeiboLoginPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
        SharedPreferences.Editor editor = activity
                .getSharedPreferences(PreferenceEnum.SESSION.name(), Context.MODE_PRIVATE).edit();
        editor.remove(MyCookies.SESSION_NAME);
        editor.apply();
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
    }
}
