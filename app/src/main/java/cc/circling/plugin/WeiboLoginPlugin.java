package cc.circling.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private String clientId;

    public WeiboLoginPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("WeiboLoginPlugin: " + json.toJSONString());
        clientId = json.getString("clientId");
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
                                    for (Cookie cookie : cookies) {
                                        LogUtil.i("cookie: " + cookie.toString());
                                        MyCookies.add(cookie.name(), cookie.value());
                                        if(cookie.name().equals(MyCookies.SESSION_NAME)) {
                                            SharedPreferences.Editor editor = activity.getSharedPreferences(PreferenceEnum.SESSION.name(), Context.MODE_PRIVATE).edit();
                                            editor.putString(MyCookies.SESSION_NAME, cookie.toString());
                                            editor.apply();
                                        }
                                    }
                                }
                                @Override
                                public List<Cookie> loadForRequest(HttpUrl url) {
                                    return new ArrayList<>();
                                }
                            })
                            .build();
                        FormBody.Builder bodyBuilder = new FormBody.Builder();
                        Set<String> keys = param.keySet();
                        for(String key : keys) {
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
                        if(responseBody.isEmpty()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject json = new JSONObject();
                                    json.put("success", false);
                                    activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
                                }
                            });
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + responseBody + "');");
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
                                activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "','" + json.toJSONString() + "');");
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
