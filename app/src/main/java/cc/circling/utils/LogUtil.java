package cc.circling.utils;

import android.util.Log;

/**
 * Created by army on 2017/3/18.
 */

public class LogUtil {
    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }
    public static void v(String log) {
        Log.v("StackTraceElement", getCallerStackTraceElement().toString());
        Log.v("日志", log);
    }
    public static void v(String tag, String log) {
        Log.v("StackTraceElement", getCallerStackTraceElement().toString());
        Log.v("日志", tag + " " + log);
    }
    public static void d(String log) {
        Log.d("StackTraceElement", getCallerStackTraceElement().toString());
        Log.d("日志", log);
    }
    public static void d(String tag, String log) {
        Log.d("StackTraceElement", getCallerStackTraceElement().toString());
        Log.d("日志", tag + " " + log);
    }
    public static void i(String log) {
        Log.i("StackTraceElement", getCallerStackTraceElement().toString());
        Log.i("日志", log);
    }
    public static void i(String tag, String log) {
        Log.i("StackTraceElement", getCallerStackTraceElement().toString());
        Log.i("日志", tag + " " + log);
    }
    public static void w(String log) {
        Log.w("StackTraceElement", getCallerStackTraceElement().toString());
        Log.w("日志", log);
    }
    public static void w(String tag, String log) {
        Log.w("StackTraceElement", getCallerStackTraceElement().toString());
        Log.w("日志", tag + " " + log);
    }
    public static void e(String log) {
        Log.e("StackTraceElement", getCallerStackTraceElement().toString());
        Log.e("日志", log);
    }
    public static void e(String tag, String log) {
        Log.e("StackTraceElement", getCallerStackTraceElement().toString());
        Log.e("日志", tag + " " + log);
    }
}
