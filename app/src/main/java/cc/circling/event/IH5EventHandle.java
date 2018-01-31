package cc.circling.event;

import com.alibaba.fastjson.JSONObject;

import cc.circling.MainActivity;

/**
 * Created by army on 2017/3/20.
 */

public interface IH5EventHandle {
    void handle(JSONObject param);
    boolean isActivity(MainActivity activity);
}
