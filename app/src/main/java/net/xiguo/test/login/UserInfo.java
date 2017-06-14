package net.xiguo.test.login;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/5/24.
 */

public class UserInfo {
    private static String userId;
    private static String userName;

    public static void setUserInfo(JSONObject jsonObject) {
        LogUtil.i("setUserInfo: ", jsonObject.toJSONString());
        userId = jsonObject.getString("uid");
        userName = jsonObject.getString("nickName");
    }
    public static String getUserId() {
        return userId;
    }
    public static String getUserName() {
        return userName;
    }
}
