package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

import cc.circling.BaseApplication;
import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2018/1/25.
 */

public class GetCachePlugin extends H5Plugin {

    public GetCachePlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("GetCachePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            Boolean isArray = param.getBoolean("isArray");
            if(isArray == null) {
                isArray = false;
            }
            SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE);
            if(isArray) {
                JSONArray key = param.getJSONArray("key");
                if(key.size() > 0) {
                    StringBuilder value = new StringBuilder("[");
                    for(int i = 0; i < key.size(); i++) {
                        String k = key.getString(i);
                        String v = sharedPreferences.getString(k, "null");
                        value.append(v);
                        if(i < key.size() - 1) {
                            value.append(",");
                        }
                    }
                    value.append("]");
                    activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + value.toString() + ");", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //
                        }
                    });
                }
            }
            else {
                String key = param.getString("key");
                String value = sharedPreferences.getString(key, "null");
                activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + value + ");", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //
                    }
                });
            }
        }
    }
}
