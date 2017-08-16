package net.xiguo.test.plugin;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/5/1.
 */

public class AlertPlugin extends H5Plugin {
    public AlertPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject data) {
        LogUtil.i("AlertPlugin: " + data.toJSONString());
        JSONObject param = data.getJSONObject("param");
        if(param != null) {
            String title = param.getString("title");
            String message = param.getString("message");
            AlertDialog.Builder dialog = new AlertDialog.Builder(this.activity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                }
            });
            dialog.show();
        }
    }
}
