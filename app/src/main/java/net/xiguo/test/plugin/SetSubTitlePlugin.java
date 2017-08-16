package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class SetSubTitlePlugin extends H5Plugin {

    public SetSubTitlePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        LogUtil.i("SetSubTitlePlugin: " + data.toJSONString());
        String title = data.getString("param");
        this.activity.setSubTitle(title);
    }
}
