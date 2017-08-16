package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class SetOptionMenuPlugin extends H5Plugin {

    public SetOptionMenuPlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject data) {
        LogUtil.i("SetOptionMenuPlugin: " + data.toJSONString());
        JSONObject param = data.getJSONObject("param");
        if(param != null) {
            String text = param.getString("text");
            if(text != null && text.length() > 0) {
                activity.setOptionMenuText(text);
            }
        }
    }
}
