package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;

/**
 * Created by army8735 on 2017/8/29.
 */

public class SetCookiePlugin extends H5Plugin {

    public SetCookiePlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetCookiePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String key = param.getString("key");
            String value = param.getString("value");
            SharedPreferences.Editor editor = BaseApplication.getContext().getSharedPreferences("cookie", Context.MODE_PRIVATE).edit();
            LogUtil.i("SetCookiePlugin isNull: " + (value == null));
            if(value == null) {
                editor.remove(key);
            }
            else {
                editor.putString(key, value);
                MyCookies.add(key, value);
            }
            editor.apply();
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "',true);");
        }
    }
}
