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
        String title = data.getString("param");
        LogUtil.i("SetSubTitlePlugin: " + title);
        this.activity.setSubTitle(title);
    }
}
