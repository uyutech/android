package cc.circling;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.alibaba.fastjson.JSONObject;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cc.circling.utils.LogUtil;
import cc.circling.web.OkHttpDns;
import cc.circling.web.URLs;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by army8735 on 2018/1/15.
 */

public class MediaService extends Service {
    private static final String MEDIA_CLOSE = "cc.circling.MediaService.MEDIA_CLOSE";
    private static final String MEDIA_PLAY = "cc.circling.MediaService.MEDIA_PLAY";
    private static final String MEDIA_PAUSE = "cc.circling.MediaService.MEDIA_PAUSE";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null) {
                LogUtil.i("onReceive", action);
                switch(action) {
                    case MEDIA_CLOSE:
                        playBinder.stop();
                        break;
                    case MEDIA_PLAY:
                        playBinder.play();
                        remoteViews.setViewVisibility(R.id.play, View.GONE);
                        remoteViews.setViewVisibility(R.id.pause, View.VISIBLE);
                        builder.setCustomContentView(remoteViews);
                        notification = builder.build();
                        startForeground(1, notification);
                        break;
                    case MEDIA_PAUSE:
                        playBinder.pause();
                        remoteViews.setViewVisibility(R.id.pause, View.GONE);
                        remoteViews.setViewVisibility(R.id.play, View.VISIBLE);
                        builder.setCustomContentView(remoteViews);
                        notification = builder.build();
                        startForeground(1, notification);
                        break;
                }
            }
        }
    };

    private PlayBinder playBinder = new PlayBinder();
    private MainActivity mainActivity;
    private SimpleExoPlayer player;
    private long duration;
    private int percent;
    private boolean isPlaying = false;
    private boolean isPreparing;
    private String lastId;
    private Boolean isNew;
    private long lastPosition;
    private Timer timer;
    private TimerTask timerTask;
    private Timer timer2;
    private TimerTask timerTask2;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private RemoteViews remoteViews;

    class PlayBinder extends Binder {

        public void start(MainActivity mainActivity) {
            LogUtil.i("start");
            MediaService.this.mainActivity = mainActivity;
        }
        private void init() {
            percent = 0;
            isPlaying = false;
            isPreparing = true;
            lastPosition = 0;
            if(player != null) {
                return;
            }
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(mainActivity, trackSelector);
            player.addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                    LogUtil.d("onTimelineChanged", reason + "");
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                    LogUtil.d("onLoadingChanged", isLoading + "");
                    if(isLoading) {
                        timer2 = new Timer();
                        timerTask2 = new TimerTask() {
                            @Override
                            public void run() {
                                if(mainActivity == null || player == null) {
                                    return;
                                }
                                if(lastPosition == player.getBufferedPosition()) {
                                    return;
                                }
                                lastPosition = player.getBufferedPosition();
                                JSONObject json = new JSONObject();
                                json.put("id", lastId);
                                duration = Math.max(0, player.getDuration());
                                json.put("duration", duration);
                                json.put("position", lastPosition);
                                percent = player.getBufferedPercentage();
                                json.put("percent", percent);
                                mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaProgress', " + json.toJSONString() + ");");
                            }
                        };
                        timer2.schedule(timerTask2, 0, 100);
                    }
                    else {
                        if(timer2 != null) {
                            timer2.cancel();
                            timer2 = null;
                        }
                        if(timerTask2 != null) {
                            timerTask2.cancel();
                            timerTask2 = null;
                        }
                        if(mainActivity == null || player == null) {
                            return;
                        }
                        if(lastPosition == player.getBufferedPosition()) {
                            return;
                        }
                        lastPosition = player.getBufferedPosition();
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        duration = Math.max(0, player.getDuration());
                        json.put("duration", duration);
                        json.put("position", lastPosition);
                        percent = player.getBufferedPercentage();
                        json.put("percent", percent);
                        mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaProgress', " + json.toJSONString() + ");");
                    }
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    LogUtil.d("onPlayerStateChanged", lastId + ", " + playWhenReady + ", " + playbackState);
                    if(isPreparing && playbackState == Player.STATE_READY) {
                        isPreparing = false;
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        duration = Math.max(0, player.getDuration());
                        json.put("duration", duration);
                        mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaPrepared', " + json.toJSONString() + ");");
                    }
                    // 发现出现2回调2次的情况，附加isPlaying以判断去重
                    else if(isPlaying && playbackState == Player.STATE_ENDED) {
                        isPlaying = false;
                        player.setPlayWhenReady(false);
                        player.seekTo(0);
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaEnd', " + json.toJSONString() + ");");
                    }
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    LogUtil.d("onRepeatModeChanged", repeatMode + "");
                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                    LogUtil.d("onShuffleModeEnabledChanged", shuffleModeEnabled + "");
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    LogUtil.e("onPlayerError", error.toString());
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    LogUtil.d("onPositionDiscontinuity", reason + "");
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                }

                @Override
                public void onSeekProcessed() {
                }
            });
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(mainActivity == null || player == null || player.getPlaybackState() != Player.STATE_READY || !isPlaying) {
                        return;
                    }
                    JSONObject json = new JSONObject();
                    json.put("id", lastId);
                    json.put("currentTime", player.getCurrentPosition());
                    duration = Math.max(0, player.getDuration());
                    json.put("duration", duration);
                    json.put("percent", percent);
                    mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaTimeupdate', " + json.toJSONString() + ");");
                }
            };
            timer.schedule(timerTask, 0, 100);
        }
        public void info(JSONObject value) {
            LogUtil.i("info", value.toJSONString());
            String url = value.getString("url");
            String id = value.getString("id");
            String cover = value.getString("cover");
            String title = value.getString("title");
            String author = value.getString("author");
            // 传入和上次相同的信息时忽略
            if(lastId != null && lastId.equals(id)) {
                return;
            }
            this.init();

            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            if(url.startsWith(URLs.LOCAL_DOMAIN)) {
                url = url.substring(URLs.LOCAL_DOMAIN.length());
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mainActivity,
                        Util.getUserAgent(mainActivity, "cc.circling"), bandwidthMeter);
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url));
                player.prepare(mediaSource);
            }
            else {
                File cacheFile;
                long maxSize;
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    cacheFile = new File(mainActivity.getExternalCacheDir(), "media");
                    maxSize = 1024 * 1024 * 1024;
                }
                else {
                    cacheFile = new File(mainActivity.getCacheDir(), "media");
                    maxSize = 200 * 1024 * 1024;
                }
                LogUtil.d("cacheFile", maxSize + ", " + cacheFile.toString());
                OkHttpClient client = new OkHttpClient
                        .Builder()
                        .dns(OkHttpDns.getInstance())
                        .cache(new Cache(cacheFile, maxSize))
                        .build();
                OkHttpDataSourceFactory dataSourceFactory = new OkHttpDataSourceFactory(client,
                        Util.getUserAgent(mainActivity, "cc.circling"), bandwidthMeter);
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url));
                player.prepare(mediaSource);
            }
            lastId = id;
            isNew = true;

            remoteViews = new RemoteViews(getPackageName(), R.layout.notification_media);
            remoteViews.setImageViewResource(R.id.icon, R.mipmap.ic_launcher);
            remoteViews.setTextViewText(R.id.title, title);
            remoteViews.setTextViewText(R.id.content, author);
            Intent intentClose = new Intent(MEDIA_CLOSE);
//            Bundle bundleClose = new Bundle();
//            bundleClose.putInt("type", MEDIA_CLOSE);
//            intentClose.putExtras(bundleClose);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MediaService.this, 0, intentClose, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.close, pendingIntent);
            Intent intentPause = new Intent(MEDIA_PAUSE);
//            Bundle bundlePause = new Bundle();
//            bundlePause.putInt("type", MEDIA_PAUSE);
//            intentPause.putExtras(bundlePause);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(MediaService.this, 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.pause, pendingIntent2);
            Intent intentPlay = new Intent(MEDIA_PLAY);
//            Bundle bundlePlay = new Bundle();
//            bundlePlay.putInt("type", MEDIA_PLAY);
//            intentPlay.putExtras(bundlePlay);
            PendingIntent pendingIntent3 = PendingIntent.getBroadcast(MediaService.this, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.play, pendingIntent3);
            builder = new NotificationCompat.Builder(MediaService.this, "media");
            builder.setWhen(System.currentTimeMillis());
            builder.setTicker("正在播放：" + title);
            builder.setContentTitle(title);
            builder.setContentText(author);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.ic_launcher));
            builder.setCustomContentView(remoteViews);

            if(cover != null && !cover.isEmpty()) {
                downloadCover(cover);
            }
        }
        public void play() {
            play(null, null);
        }
        public void play(JSONObject value, String clientId) {
            LogUtil.i("play", clientId);
            if(value != null) {
                info(value);
            }
            if(player != null) {
                isPlaying = true;
                player.setPlayWhenReady(true);
            }
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
            if(isNew) {
                isNew = false;
                if(notification != null) {
                    stopForeground(true);
                }
                notification = builder.build();
                startForeground(1, notification);
            }
        }
        public void pause() {
            pause(null);
        }
        public void pause(String clientId) {
            LogUtil.i("pause", clientId);
            isPlaying = false;
            if(player != null) {
                player.setPlayWhenReady(false);
            }
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
        }
        public void stop() {
            stop(null);
        }
        public void stop(String clientId) {
            LogUtil.i("stop", clientId);
            isPlaying = false;
            isPreparing = true;
            lastPosition = 0;
            if(player != null) {
                player.stop();
            }
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
            lastId = null;
            if(notification != null) {
                stopForeground(true);
                notification = null;
            }
        }
        public void release(String clientId) {
            LogUtil.i("release", clientId);
            percent = 0;
            isPlaying = false;
            isPreparing = true;
            lastPosition = 0;
            if(player != null) {
                player.stop();
            }
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
            if(timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            if(timer2 != null) {
                timer2.cancel();
                timer2 = null;
            }
            if(timerTask2 != null) {
                timerTask2.cancel();
                timerTask2 = null;
            }
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
            lastId = null;
            if(notification != null) {
                stopForeground(true);
                notification = null;
            }
        }
        public void seek(JSONObject value, String clientId) {
            long time = value.getInteger("time");
            LogUtil.i("seek", time + ", " + clientId);
            player.seekTo(time);
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i("onBind");
        return playBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i("onUnbind");
        return super.onUnbind(intent);
    }
    @Override
    public void onCreate() {
        LogUtil.i("onCreate");
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MEDIA_CLOSE);
        intentFilter.addAction(MEDIA_PLAY);
        intentFilter.addAction(MEDIA_PAUSE);
        registerReceiver(broadcastReceiver, intentFilter);
    }
    @Override
    public void onDestroy() {
        LogUtil.i("onDestroy");
        super.onDestroy();
        lastId = null;
        percent = 0;
        isPlaying = false;
        isPreparing = true;
        lastPosition = 0;
        if(player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if(timer2 != null) {
            timer2.cancel();
            timer2 = null;
        }
        if(timerTask2 != null) {
            timerTask2.cancel();
            timerTask2 = null;
        }
        if(remoteViews != null) {
            remoteViews = null;
        }
        unregisterReceiver(broadcastReceiver);
    }
    private void downloadCover(String url) {
        LogUtil.i("downloadCover", url);
        String id = lastId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient
                        .Builder()
                        .dns(OkHttpDns.getInstance())
                        .build();
                    Request request = new Request.Builder()
                        .get()
                        .url(url)
                        .build();
                    Response response = client.newCall(request).execute();
                    LogUtil.i("downloadCover res", response.toString());
                    if(response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if(responseBody == null) {
                            return;
                        }
                        byte[] data = responseBody.bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if(bitmap == null) {
                            return;
                        }
                        LogUtil.i("downloadCover fin", bitmap.toString());
                        if(lastId == null) {
                            return;
                        }
                        if(!lastId.equals(id)) {
                            return;
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!lastId.equals(id)) {
                                    return;
                                }
                                remoteViews.setImageViewBitmap(R.id.icon, bitmap);
                                notification = builder.build();
                                startForeground(1, notification);
                            }
                        });
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
