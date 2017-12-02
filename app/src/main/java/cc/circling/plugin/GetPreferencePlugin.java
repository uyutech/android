package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2017/8/6.
 */

public class GetPreferencePlugin extends H5Plugin {

    public GetPreferencePlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject data) {
        LogUtil.i("GetPreferencePlugin: " + data.toJSONString());
        String clientId = data.getString("clientId");
        JSONObject param = data.getJSONObject("param");
        if(param != null) {
            String key = param.getString("key");
            SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE);
            String value = sharedPreferences.getString(key, "");
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + value + "');");
        }
    }
}
