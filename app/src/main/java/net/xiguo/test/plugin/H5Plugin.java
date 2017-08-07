package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.event.IH5EventHandle;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/23.
 */

public abstract class H5Plugin implements IH5EventHandle {
    public static final String SET_TITLE = "setTitle";
    public static final String PUSH_WINDOW = "pushWindow";
    public static final String POP_WINDOW = "popWindow";
    public static final String BACK = "back";
    public static final String TOAST = "toast";
    public static final String SHOW_LOADING = "showLoading";
    public static final String HIDE_LOADING = "hideLoading";
    public static final String ALERT = "alert";
    public static final String CONFIRM = "confirm";
    public static final String HIDE_BACKBUTTON = "hideBackButton";
    public static final String SHOW_BACKBUTTON = "showBackButton";
    public static final String USER_INFO = "userInfo";
    public static final String SWIPE_REFRESH = "swipeRefresh";
    public static final String LOGIN_WEIBO = "loginWeibo";
    public static final String GET_PRE_FERENCE = "getPreference";
    public static final String SET_PRE_FERENCE = "setPreference";

    protected X5Activity activity;

    public H5Plugin(X5Activity activity) {
        this.activity = activity;
    }

    public abstract void handle(JSONObject param);

    public boolean isActivity(X5Activity activity) {
        return this.activity == activity;
    }
}
