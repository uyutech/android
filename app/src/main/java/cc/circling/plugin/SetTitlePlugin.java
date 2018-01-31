package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/3/23.
 */

public class SetTitlePlugin extends H5Plugin {

    public SetTitlePlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetTitlePlugin: " + json.toJSONString());
        String title = json.getString("param");
        LogUtil.i("SetTitlePlugin: " + title);
        this.activity.setTitle(title);
    }
}
