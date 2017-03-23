package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/23.
 */

public class SetTitlePlugin extends H5Plugin {
    private X5Activity activity;

    public SetTitlePlugin(X5Activity activity) {
        this.activity = activity;
    }

    @Override
    public void handle(JSONObject param) {
        super.handle(param);
        String title = param.getString("param");
        LogUtil.i("SetTitlePlugin: " + title);
        this.activity.setTitle(title);
    }
}
