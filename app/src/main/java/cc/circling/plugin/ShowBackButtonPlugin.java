package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/5/2.
 */

public class ShowBackButtonPlugin extends H5Plugin {
    public ShowBackButtonPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("ShowBackButtonPlugin: " + json.toJSONString());
        this.activity.showBackButton();
    }
}
