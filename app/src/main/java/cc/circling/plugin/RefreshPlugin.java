package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/6/13.
 */

public class RefreshPlugin extends H5Plugin {
    public RefreshPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("RefreshPlugin: " + json.toJSONString());
        JSONObject p = json.getJSONObject("param");
        if(p != null) {
            boolean prevent = p.getBoolean("prevent");
            if(!prevent) {
                activity.getWebView().reload();
            }
            activity.getSwipeRefreshLayout().setRefreshing(false);
        }
    }
}
