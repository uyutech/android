package net.xiguo.test.plugin;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/4/18.
 */

public class PopWindowPlugin extends H5Plugin {

    public PopWindowPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        LogUtil.i("PopWindowPlugin: " + data.toJSONString());
        Intent intent = new Intent();
        intent.putExtra("param", data.getString("param"));
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }
}
