package net.xiguo.test.web;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by army on 2017/3/25.
 */

public class MyCookies {
    public static final String COOKIE_NAME = "sessionid";

    private static ArrayList<String> cookies = new ArrayList();

    public MyCookies() {
    }

    public static void add(String cookie) {
        cookies.add(cookie);
    }
    public static ArrayList<String> getAll() {
        return cookies;
    }
}
