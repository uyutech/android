package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/8/10.
 */

public class SetOptionMenuPlugin extends H5Plugin {

    public SetOptionMenuPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetOptionMenuPlugin: " + json.toJSONString());
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String text = param.getString("text");
            String textColor = param.getString("textColor");
            if(textColor == null || textColor.length() == 0) {
                textColor = "#000000";
            }
            String img1 = param.getString("img1");
            String img2 = param.getString("img2");
            activity.setOptionMenuText(text, textColor);
            activity.setOptionMenuImg1(img1);
            activity.setOptionMenuImg2(img2);
        }
    }
}
