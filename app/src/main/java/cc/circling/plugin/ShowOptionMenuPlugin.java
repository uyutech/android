package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class ShowOptionMenuPlugin extends H5Plugin {

    public ShowOptionMenuPlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("ShowOptionMenuPlugin: " + json.toJSONString());
        activity.showOptionMenu();
    }
}
