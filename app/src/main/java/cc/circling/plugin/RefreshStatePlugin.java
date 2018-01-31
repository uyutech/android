package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/8.
 */

public class RefreshStatePlugin extends H5Plugin {
    public RefreshStatePlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("RefreshStatePlugin: " + json.toJSONString());
        boolean p = json.getBoolean("param");
//        this.activity.getSwipeRefreshLayout().setCanEnabled(p);
    }
}
