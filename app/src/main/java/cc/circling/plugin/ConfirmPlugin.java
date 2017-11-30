package cc.circling.plugin;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

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
        final String clientId = param.getString("clientId");
        JSONObject p = param.getJSONObject("param");
        if(p != null) {
            String title = p.getString("title");
            String message = p.getString("message");
            AlertDialog.Builder dialog = new AlertDialog.Builder(this.activity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    ConfirmPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "',true);");
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    ConfirmPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "',false);");
                }
            });
            dialog.show();
        }
    }
}
