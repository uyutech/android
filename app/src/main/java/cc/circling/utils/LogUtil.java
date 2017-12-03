package cc.circling.utils;

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
        Log.i("i日志" + getCallerStackTraceElement(), tag + " " + log);
    }
    public static void d(String log) {
        Log.d("d日志" + getCallerStackTraceElement(), log);
    }
    public static void w(String log) {
        Log.w("w日志" + getCallerStackTraceElement(), log);
    }
    public static void w(String tag, String log) {
        Log.w("w日志" + getCallerStackTraceElement(), tag + " " + log);
    }
    public static void e(String log) {
        Log.e("e日志" + getCallerStackTraceElement(), log);
    }
    public static void e(String tag, String log) {
        Log.e("e日志" + getCallerStackTraceElement(), tag + " " + log);
    }
}
