package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2018/1/25.
 */

public class GetCachePlugin extends H5Plugin {

    public GetCachePlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("GetCachePlugin: " + json.toJSONString());
        String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if (param != null) {

        }
    }
}
