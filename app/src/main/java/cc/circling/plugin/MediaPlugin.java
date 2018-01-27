package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2018/1/15.
 */

public class MediaPlugin extends H5Plugin {

    public MediaPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("MediaPlugin: " + json.toJSONString());
        JSONObject param = json.getJSONObject("param");
        String clientId = json.getString("clientId");
        if(param != null) {
            String key = param.getString("key");
            JSONObject value = param.getJSONObject("value");
            this.activity.media(key, value, clientId);
        }
    }
}