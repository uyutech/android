package net.xiguo.test.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.BaseApplication;
import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/6.
 */

public class GetPreferencePlugin extends H5Plugin {

    public GetPreferencePlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("GetPreferencePlugin: " + params);
        String clientId = param.getString("clientId");
        JSONObject p = param.getJSONObject("param");
        if(p != null) {
            String key = p.getString("key");
            SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences("global", Context.MODE_PRIVATE);
            String sessionid = sharedPreferences.getString(key, "");
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + sessionid + "');");
        }
    }
}
