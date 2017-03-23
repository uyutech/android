package net.xiguo.test.event;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by army on 2017/3/20.
 */

public interface IH5EventHandle {
    void handle(JSONObject param);
}
