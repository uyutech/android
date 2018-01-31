package cc.circling.plugin;

import android.view.Gravity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/4/29.
 */

public class ToastPlugin extends H5Plugin {

    public ToastPlugin(MainActivity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("ToastPlugin: " + json.toJSONString());
        String s = json.getString("param");
        Toast toast = Toast.makeText(this.activity, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
