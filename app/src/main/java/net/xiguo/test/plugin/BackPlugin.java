package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/27.
 */

public class BackPlugin extends H5Plugin {

    public BackPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("BackPlugin: " + params);
        JSONObject p = param.getJSONObject("param");
        if(p != null) {
            boolean prevent = p.getBoolean("prevent");
            if(!prevent) {
                if(this.activity.getWebView().canGoBack()) {
                    this.activity.getWebView().goBack();
                }
                else if(this.activity.isFirstWeb()) {
                    this.activity.moveTaskToBack(true);
                }
                else {
                    this.activity.finish();
                }
            }
        }
    }
}
