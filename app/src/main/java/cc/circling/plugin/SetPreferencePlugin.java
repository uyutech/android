package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2017/8/6.
 */

public class SetPreferencePlugin extends H5Plugin {

    public SetPreferencePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetPreferencePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String key = param.getString("key");
            String value = param.getString("value");
            SharedPreferences.Editor editor = BaseApplication
                    .getContext()
                    .getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE).edit();
            LogUtil.i("SetPreferencePlugin: " + (value == null));
            editor.remove(key);
            if(value != null) {
                editor.putString(key, value);
            }
            editor.apply();
            activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "');", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
        }
    }
}
