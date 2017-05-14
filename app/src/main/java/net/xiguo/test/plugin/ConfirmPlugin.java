package net.xiguo.test.plugin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.DialogPreference;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/5/1.
 */

public class ConfirmPlugin extends H5Plugin {
    public ConfirmPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("ConfirmPlugin: " + params);
        JSONObject p = param.getJSONObject("param");
        if(p != null) {
            String title = p.getString("title");
            String message = p.getString("message");
            final String uid = p.getString("uid");
            AlertDialog.Builder dialog = new AlertDialog.Builder(this.activity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    ConfirmPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge.confirmCb('" + uid + "',true);");
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    ConfirmPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge.confirmCb('" + uid + "',false);");
                }
            });
            dialog.show();
        }
    }
}
