package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.event.IH5EventHandle;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/23.
 */

public class H5Plugin implements IH5EventHandle {
    public static final String SET_TITLE = "setTitle";

    public void handle(JSONObject param) {
        LogUtil.i("H5Plugin: " + param.toString());
    }
}
