package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/23.
 */

public class SetTitlePlugin extends H5Plugin {

    public SetTitlePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        String title = data.getString("param");
        LogUtil.i("SetTitlePlugin: " + title);
        this.activity.setTitle(title);
    }
}
