package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/5/2.
 */

public class HideBackButtonPlugin extends H5Plugin {
    public HideBackButtonPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("HideBackButtonPlugin: " + json.toJSONString());
        this.activity.hideBackButton();
    }
}
