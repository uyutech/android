package cc.circling.plugin;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;
import cc.circling.event.IH5EventHandle;

/**
 * Created by army on 2017/3/23.
 */

public abstract class H5Plugin implements IH5EventHandle {
    public static final String SET_TITLE = "setTitle";
    public static final String SET_SUB_TITLE = "setSubTitle";
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
    public static final String REFRESH = "refresh";
    public static final String REFRESH_STATE = "refreshState";
    public static final String LOGIN_WEIBO = "loginWeibo";
    public static final String GET_PRE_FERENCE = "getPreference";
    public static final String SET_PRE_FERENCE = "setPreference";
    public static final String SHOW_OPTIONMENU = "showOptionMenu";
    public static final String HIDE_OPTIONMENU = "hideOptionMenu";
    public static final String SET_OPTIONMENU = "setOptionMenu";
    public static final String SET_TITLE_BG_COLOR = "setTitleBgColor";
    public static final String MOVE_TASK_TO_BACK = "moveTaskToBack";
    public static final String OPEN_URI = "openUri";
    public static final String SET_COOKIE = "setCookie";
    public static final String WEIBO_LOGIN = "weiboLogin";
    public static final String LOGIN_OUT = "loginOut";
    public static final String NOTIFY = "notify";
    public static final String ALBUM = "album";
    public static final String PROMPT = "prompt";
    public static final String DOWNLOAD = "download";

    protected X5Activity activity;

    public H5Plugin(X5Activity activity) {
        this.activity = activity;
    }

    public abstract void handle(JSONObject param);

    public boolean isActivity(X5Activity activity) {
        return this.activity == activity;
    }
}
