package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cc.circling.BaseApplication;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;
import cc.circling.web.MyCookies;
import cc.circling.web.PreferenceEnum;
import cc.circling.web.URLs;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by army8735 on 2017/12/2.
 */

public class WeiboLoginPlugin extends H5Plugin {

    public WeiboLoginPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("WeiboLoginPlugin: " + json.toJSONString());
        final String clientId = json.getString("clientId");
        final JSONObject param = json.getJSONObject("param");
        if(param != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.i("weiboLogin run");
                    try {
                        OkHttpClient client = new OkHttpClient
                                .Builder()
                                .cookieJar(new CookieJar() {
                                    @Override
                                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                        SharedPreferences.Editor editor = activity.getSharedPreferences(PreferenceEnum.SESSION.name(), Context.MODE_PRIVATE).edit();
                                        for (Cookie cookie : cookies) {
                                            LogUtil.i("cookie string: " + cookie.toString());
                                            MyCookies.add(cookie.name(), cookie.toString());
                                            editor.putString(cookie.name(), cookie.toString());
                                        }
                                        editor.apply();
                                    }

                                    @Override
                                    public List<Cookie> loadForRequest(HttpUrl url) {
                                        return new ArrayList<>();
                                    }
                                })
                                .build();
                        FormBody.Builder bodyBuilder = new FormBody.Builder();
                        Set<String> keys = param.keySet();
                        for (String key : keys) {
                            String value = param.getString(key);
                            bodyBuilder.add(key, value);
                            LogUtil.i("param: " + key + ", " + value);
                        }
                        RequestBody requestBody = bodyBuilder.build();
                        Request request = new Request.Builder()
                                .addHeader("origin", URLs.WEB_DOMAIN)
                                .url(URLs.H5_DOMAIN + "/h5/oauth/weibo")
                                .post(requestBody)
                                .build();
                        LogUtil.i("weiboLogin: " + URLs.H5_DOMAIN + "/h5/oauth/weibo");
                        Response response = client.newCall(request).execute();
                        final String responseBody = response.body().string();
                        LogUtil.i("weiboLogin: " + responseBody);
                        if (responseBody.isEmpty()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject json = new JSONObject();
                                    json.put("success", false);
                                    activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json.toJSONString() + ");", new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String value) {
                                            //
                                        }
                                    });
                                }
                            });
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.syncCookie();
                                JSONObject json = JSONObject.parseObject(responseBody);
                                if (json.getBoolean("success")) {
                                    JSONObject data = json.getJSONObject("data");
                                    if (data != null) {
                                        JSONObject userInfo = data.getJSONObject("userInfo");
                                        if (userInfo != null) {
                                            String uid = userInfo.getString("UID");
                                            LogUtil.i("weiboLogin: " + uid);
                                            MobclickAgent.onProfileSignIn(uid);
                                            CrashReport.setUserId(uid);
                                            BaseApplication.getCloudPushService().bindAccount(uid, new CommonCallback() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    LogUtil.i("bindAccount success", message);
                                                }

                                                @Override
                                                public void onFailed(String message, String arg) {
                                                    LogUtil.i("bindAccount fail", message + ", " + arg);
                                                }
                                            });
                                        }
                                    }
                                }
                                activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + responseBody + ");", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        //
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.i("weiboLogin exception", e.toString());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject json = new JSONObject();
                                json.put("success", false);
                                activity.getWebView().evaluateJavascript("ZhuanQuanJSBridge._invokeJS('" + clientId + "'," + json.toJSONString() + ");", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        //
                                    }
                                });
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
