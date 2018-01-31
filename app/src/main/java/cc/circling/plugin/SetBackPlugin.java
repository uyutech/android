package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2018/1/17.
 */

public class SetBackPlugin extends H5Plugin {

    public SetBackPlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("SetBackPlugin: " + json.toJSONString());
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String img = param.getString("img");
            if(img != null && img.length() > 0) {
//                activity.setBackIcon(img);
            }
        }
    }
}
