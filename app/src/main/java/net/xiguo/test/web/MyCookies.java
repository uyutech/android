package net.xiguo.test.web;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by army on 2017/3/25.
 */

public class MyCookies {
    public static final String COOKIE_NAME = "sessionid";
    private static ArrayList<String> cookies = new ArrayList();

    private static HashMap<String, String> cookieMap = new HashMap<>();

    private MyCookies() {
    }

    public static void add(String cookie) {
        cookies.add(cookie);
    }

    public static void add(String key, String value) {
        cookieMap.put(key, value);
    }
    public static String get(String key) {
        return cookieMap.get(key);
    }
    public static ArrayList<String> getAll() {
        ArrayList<String> list = new ArrayList<>();
        for(String key : cookieMap.keySet()) {
            list.add(key + "=" + cookieMap.get(key));
        }
        return list;
    }
}
