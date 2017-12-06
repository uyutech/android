package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.PreferenceEnum;

import static android.content.Context.MODE_PRIVATE;

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
        LogUtil.i("LoginOutPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PreferenceEnum.SESSION.name(), MODE_PRIVATE);
        SharedPreferences.Editor editor = activity
                .getSharedPreferences(PreferenceEnum.SESSION.name(), MODE_PRIVATE).edit();
        Map<String, ?> map = sharedPreferences.getAll();
        for(String key : map.keySet()) {
            String cookie = map.get(key).toString();
            MyCookies.remove(key);
            editor.remove(key);
            LogUtil.i(key, cookie);
        }
        editor.apply();
        activity.syncCookie();
        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MobclickAgent.onProfileSignOff();
            }
        });
    }
}
