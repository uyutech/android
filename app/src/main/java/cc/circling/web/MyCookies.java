package cc.circling.web;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by army on 2017/3/25.
 */

public class MyCookies {
    public static final String SESSION_NAME = "sessionid";

    private static HashMap<String, String> cookieMap = new HashMap<>();

    private MyCookies() {
    }

    public static void add(String key, String value) {
        cookieMap.put(key, value);
    }
    public static String get(String key) {
        return cookieMap.get(key);
    }
    public static HashMap<String, String> getAll() {
        return cookieMap;
    }
}
