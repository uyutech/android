package cc.circling.plugin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;

import cc.circling.R;
import cc.circling.MainActivity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/5.
 */

public class NotifyPlugin extends H5Plugin {
    private static int uid = 0;
    public NotifyPlugin(MainActivity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("NotificationPlugin: " + json.toJSONString());
        final String clientId = json.getString("clientId");
        JSONObject p = json.getJSONObject("param");
        if(p != null) {
            JSONObject data = p.getJSONObject("data");
            String ticker = data.getString("ticker");
            String title = data.getString("title");
            String content = data.getString("content");
            String url = data.getString("url");
            if(title == null) {
                title = "";
            }
            if(ticker == null || ticker.equals("")) {
                ticker = title;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
            builder.setTicker(ticker);
            builder.setContentTitle(title);
            if(content != null) {
                builder.setContentText(content);
            }
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher));

            if(url != null && !url.equals("")) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("__url__", url);
                JSONObject params = p.getJSONObject("params");
                if(params != null) {
                    for(String key : params.keySet()) {
                        String value = params.getString(key);
                        intent.putExtra(key, value);
                    }
                }
                PendingIntent pIntent = PendingIntent.getActivity(activity, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pIntent);
            }

            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE);
            Notification notification = builder.build();

            NotificationManager notificationManager =
                    (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(uid++, notification);
        }
    }
}
