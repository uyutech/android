package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

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
            SharedPreferences.Editor editor = BaseApplication.getContext().getSharedPreferences("h5", Context.MODE_PRIVATE).edit();
            LogUtil.i("SetPreferencePlugin: " + (value == null));
            if(value == null) {
                editor.remove(key);
            }
            else {
                editor.putString(key, value);
            }
            editor.apply();
            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "',true);");
        }
    }
}
