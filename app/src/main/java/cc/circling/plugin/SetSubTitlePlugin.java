package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class SetSubTitlePlugin extends H5Plugin {

    public SetSubTitlePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetSubTitlePlugin: " + json.toJSONString());
        String title = json.getString("param");
        this.activity.setSubTitle(title);
    }
}
