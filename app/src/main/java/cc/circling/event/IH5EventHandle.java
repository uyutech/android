package cc.circling.event;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;

/**
 * Created by army on 2017/3/20.
 */

public interface IH5EventHandle {
    void handle(JSONObject param);
    boolean isActivity(X5Activity activity);
}
