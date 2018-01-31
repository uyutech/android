package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;

import cc.circling.BaseApplication;
import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;
import cc.circling.web.PreferenceEnum;

/**
 * Created by army8735 on 2017/8/6.
 */

public class GetPreferencePlugin extends H5Plugin {

    public GetPreferencePlugin(MainActivity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("GetPreferencePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        String param = json.getString("param");
        if(param != null && !param.equals("")) {
            SharedPreferences sharedPreferences = BaseApplication.getContext().getSharedPreferences(PreferenceEnum.H5OFF.name(), Context.MODE_PRIVATE);
            String value = sharedPreferences.getString(param, "null");
            activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + value + ");", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
        }
    }
}
