package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/27.
 */

public class BackPlugin extends H5Plugin {

    public BackPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("BackPlugin: " + json.toJSONString());
        JSONObject p = json.getJSONObject("param");
        if(p != null) {
            boolean prevent = p.getBoolean("prevent");
            if(!prevent) {
                LogUtil.i("BackPlugin: " + this.activity.getWebView().canGoBack());
                if(this.activity.getWebView().canGoBack()) {
                    this.activity.getWebView().goBack();
                }
                else {
                    this.activity.finish();
                }
            }
        }
    }
}
