package cc.circling.plugin;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/4/18.
 */

public class PopWindowPlugin extends H5Plugin {

    public PopWindowPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("PopWindowPlugin: " + json.toJSONString());
        Intent intent = new Intent();
        intent.putExtra("param", json.getString("param"));
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }
}
