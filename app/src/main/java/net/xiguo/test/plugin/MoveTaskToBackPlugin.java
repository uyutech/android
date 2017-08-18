package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/18.
 */

public class MoveTaskToBackPlugin extends H5Plugin {

    public MoveTaskToBackPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        LogUtil.i("MoveTaskToBackPlugin: " + data.toJSONString());
        activity.moveTaskToBack(true);
    }
}
