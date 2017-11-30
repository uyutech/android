package cc.circling.event;

import com.alibaba.fastjson.JSONObject;

import cc.circling.X5Activity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by army on 2017/3/22.
 */

public class H5EventDispatcher {
    private static HashMap<String, ArrayList<IH5EventHandle>> map = new HashMap();

    public static void dispatch(String action, X5Activity activity, JSONObject param) {
        if(map.containsKey(action)) {
            ArrayList<IH5EventHandle> list = map.get(action);
            for(IH5EventHandle handle : list) {
                if(handle.isActivity(activity)) {
                    handle.handle(param);
                }
            }
        }
    }
    public static void dispatch(X5Activity activity, JSONObject json) {
        String action = json.getString("fn");
        dispatch(action, activity, json);
    }
    public static void addEventListener(String action, IH5EventHandle handle) {
        if(!map.containsKey(action)) {
            map.put(action, new ArrayList<IH5EventHandle>());
        }
        ArrayList<IH5EventHandle> list = map.get(action);
        if(!list.contains(handle)) {
            list.add(handle);
        }
    }
}
