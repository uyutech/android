package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/6/13.
 */

public class SwipeRefreshPlugin extends H5Plugin {
    public SwipeRefreshPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SwipeRefreshPlugin: " + json.toJSONString());
        boolean p = json.getBoolean("param");
        this.activity.getSwipeRefreshLayout().setCanEnabled(p);
    }
}
