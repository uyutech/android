package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/18.
 */

public class MoveTaskToBackPlugin extends H5Plugin {

    public MoveTaskToBackPlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("MoveTaskToBackPlugin: " + json.toJSONString());
        activity.moveTaskToBack(true);
    }
}
