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
    public void handle(JSONObject json) {
        LogUtil.i("GetPreferencePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        String param = json.getString("param");
        if(param != null && !param.equals("")) {
            LogUtil.i("GetPreferencePlugin k: " + param);
            SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE);
            String value = sharedPreferences.getString(param, "");
            LogUtil.i("GetPreferencePlugin v: " + value);
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + value + "');");
        }
    }
}
