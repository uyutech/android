package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/4/30.
 */

public class HideLoadingPlugin extends H5Plugin {

    public HideLoadingPlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("ShowLoadingPlugin: " + params);
        if(ShowLoadingPlugin.lastProgressDialog != null) {
            ShowLoadingPlugin.lastProgressDialog.dismiss();
        }
    }
}
