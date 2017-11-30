package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

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
