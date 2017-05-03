package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/5/2.
 */

public class HideBackButtonPlugin extends H5Plugin {
    public HideBackButtonPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("HideBackButtonPlugin: " + params);
        this.activity.hideBackButton();
    }
}
