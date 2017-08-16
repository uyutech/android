package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/15.
 */

public class SetTitleBgColorPlugin extends H5Plugin {

    public SetTitleBgColorPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        LogUtil.i("SetTitleBgColorPlugin: " + data.toJSONString());
        String param = data.getString("param");
        activity.setTitleBgColor(param);
    }
}
