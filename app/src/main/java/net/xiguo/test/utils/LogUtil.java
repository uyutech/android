package net.xiguo.test.utils;

import android.util.Log;

/**
 * Created by army on 2017/3/18.
 */

public class LogUtil {
    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }
    public static void i(String log) {
        Log.i("i日志" + getCallerStackTraceElement(), log);
    }
    public static void i(String tag, String log) {
        Log.i("i日志" + getCallerStackTraceElement(), tag + log);
    }
    public static void d(String log) {
        Log.d("d日志" + getCallerStackTraceElement(), log);
    }
}
