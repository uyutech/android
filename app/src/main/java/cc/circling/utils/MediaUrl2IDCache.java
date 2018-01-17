package cc.circling.utils;

import java.util.HashMap;

/**
 * Created by army8735 on 2018/1/17.
 */

public class MediaUrl2IDCache {

    public static HashMap<String, String> map = new HashMap<>();

    public static void put(String k, String v) {
        map.put(k, v);
    }
    public static void remove(String k) {
        map.remove(k);
    }
    public static boolean containsKey(String k) {
        return map.containsKey(k);
    }
    public static String get(String k) {
        return map.get(k);
    }
}
