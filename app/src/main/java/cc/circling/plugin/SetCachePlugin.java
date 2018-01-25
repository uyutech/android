package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2018/1/25.
 */

public class SetCachePlugin extends H5Plugin {

    public SetCachePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetCachePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            Boolean isArray = param.getBoolean("isArray");
            if(isArray == null) {
                isArray = false;
            }
            SharedPreferences.Editor editor = BaseApplication
                    .getContext()
                    .getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE).edit();
            if(isArray) {
                JSONArray key = param.getJSONArray("key");
                JSONArray value = param.getJSONArray("value");
                if(key.size() > 0) {
                    for(int i = 0; i < key.size(); i++) {
                        String k = key.getString(i);
                        String v = value.getString(i);
                        LogUtil.i("setCache value is null: " + (v == null));
                        editor.remove(k);
                        if(v != null) {
                            editor.putString(k, v);
                        }
                    }
                    editor.apply();
                }
            }
            else {
                String key = param.getString("key");
                String value = param.getString("value");
                LogUtil.i("setCache value is null: " + (value == null));
                editor.remove(key);
                if(value != null) {
                    editor.putString(key, value);
                }
                editor.apply();
            }
            activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "');", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
        }
    }
}
