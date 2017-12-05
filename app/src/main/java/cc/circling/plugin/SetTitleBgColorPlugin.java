package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/15.
 */

public class SetTitleBgColorPlugin extends H5Plugin {

    public SetTitleBgColorPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetTitleBgColorPlugin: " + json.toJSONString());
        String param = json.getString("param");
        activity.setTitleBgColor(param);
    }
}
