package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.login.UserInfo;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/5/24.
 */

public class UserInfoPlugin extends H5Plugin {
    public UserInfoPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("UserInfoPlugin: " + params);
        String clientId = param.getString("clientId");
        if(clientId != null && !clientId.isEmpty()) {
            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", UserInfo.getUserId());
            userInfo.put("userName", UserInfo.getUserName());
            UserInfoPlugin.this.activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "', '" + userInfo.toString() + "');");
        }
    }
}
