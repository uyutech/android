package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/4/30.
 */

public class HideLoadingPlugin extends H5Plugin {

    public HideLoadingPlugin(MainActivity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("HideLoadingPlugin: " + json.toJSONString());
        if(ShowLoadingPlugin.lastProgressDialog != null) {
            ShowLoadingPlugin.lastProgressDialog.dismiss();
        }
    }
}
