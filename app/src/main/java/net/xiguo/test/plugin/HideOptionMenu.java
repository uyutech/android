package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class HideOptionMenu extends H5Plugin {

    public HideOptionMenu(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("HideOptionMenu: " + params);
        activity.hideOptionMenu();
    }
}
