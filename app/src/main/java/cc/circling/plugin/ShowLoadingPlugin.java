package cc.circling.plugin;

import android.app.ProgressDialog;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/4/29.
 */

public class ShowLoadingPlugin extends H5Plugin {
    public static ProgressDialog lastProgressDialog;

    public ShowLoadingPlugin(X5Activity activity) {
        super(activity);
    }
    @Override
    public void handle(JSONObject json) {
        LogUtil.i("ShowLoadingPlugin: " + json.toJSONString());
        JSONObject p = json.getJSONObject("param");
        if(p != null) {
            String title = p.getString("title");
            String message = p.getString("message");
            boolean cancelable = p.getBoolean("cancelable");
            lastProgressDialog = new ProgressDialog(this.activity);
            lastProgressDialog.setTitle(title);
            lastProgressDialog.setMessage(message);
            lastProgressDialog.setCancelable(cancelable);
            lastProgressDialog.setCanceledOnTouchOutside(false);
            lastProgressDialog.show();
        }
    }
}
