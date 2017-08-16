package net.xiguo.test.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.BaseApplication;
import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.web.MyCookies;

/**
 * Created by army8735 on 2017/8/6.
 */

public class SetPreferencePlugin extends H5Plugin {

    public SetPreferencePlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject data) {
        LogUtil.i("SetPreferencePlugin: " + data.toJSONString());
        String clientId = data.getString("clientId");
        JSONObject param = data.getJSONObject("param");
        if(param != null) {
            String key = param.getString("key");
            String value = param.getString("value");
            SharedPreferences.Editor editor = BaseApplication.getContext().getSharedPreferences("global", Context.MODE_PRIVATE).edit();
            editor.putString(key, value);
            editor.apply();
            // 特殊的sessionid
            if(key.equals(MyCookies.COOKIE_NAME)) {
                MyCookies.add("sessionid=" + value);
            }
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "',true);");
        }
    }
}
