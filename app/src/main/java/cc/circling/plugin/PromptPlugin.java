package cc.circling.plugin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/6.
 */

public class PromptPlugin extends H5Plugin {
    public PromptPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("PromptPlugin: " + json.toJSONString());
        final String clientId = json.getString("clientId");
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String title = param.getString("title");
            String message = param.getString("message");
            String value = param.getString("value");
            final EditText et = new EditText(activity);
            et.setText(value);
            et.setSelection(value.length());
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setView(et);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    String s = et.getText().toString().replaceAll("'", "\\'");
                    JSONObject json = new JSONObject();
                    json.put("success", true);
                    json.put("value", s);
                    PromptPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    JSONObject json = new JSONObject();
                    json.put("success", false);
                    PromptPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
                }
            });
            dialog.show();
        }
    }
}
