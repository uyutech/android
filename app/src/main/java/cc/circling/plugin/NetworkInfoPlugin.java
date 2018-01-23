package cc.circling.plugin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/8.
 */

public class NetworkInfoPlugin extends H5Plugin {

    public NetworkInfoPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("NetworkInfoPlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            JSONObject json2 = new JSONObject();
            json2.put("available", true);
            json2.put("wifi", wifiNetworkInfo != null & wifiNetworkInfo.isConnected() && wifiNetworkInfo.isAvailable());
            activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json2.toJSONString() + ");", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
        }
        else {
            JSONObject json2 = new JSONObject();
            json2.put("available", false);
            activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json2.toJSONString() + ");", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //
                }
            });
        }
    }
}
