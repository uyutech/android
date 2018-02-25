package cc.circling;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

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
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cc.circling.utils.LogUtil;
import cc.circling.web.OkHttpDns;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by army8735 on 2018/1/15.
 */

public class MediaService extends Service {
    private PlayBinder playBinder = new PlayBinder();
    private MainActivity mainActivity;
    private SimpleExoPlayer player;
    private long duration;
    private int percent;
    private boolean isPlaying = false;
    private String lastId;
    private Timer timer;
    private TimerTask timerTask;
    private Timer timer2;
    private TimerTask timerTask2;

    class PlayBinder extends Binder {

        public void start(MainActivity mainActivity) {
            LogUtil.i("start");
            MediaService.this.mainActivity = mainActivity;
        }
        private void init() {
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
                public void onTimelineChanged(Timeline timeline, Object manifest) {
                    LogUtil.d("onTimelineChanged", timeline.toString());
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
                                JSONObject json = new JSONObject();
                                json.put("id", lastId);
                                duration = Math.max(0, player.getDuration());
                                json.put("duration", duration);
                                json.put("position", player.getBufferedPosition());
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
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        duration = Math.max(0, player.getDuration());
                        json.put("duration", duration);
                        json.put("position", player.getBufferedPosition());
                        percent = player.getBufferedPercentage();
                        json.put("percent", percent);
                        mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaProgress', " + json.toJSONString() + ");");
                    }
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    LogUtil.d("onPlayerStateChanged", playWhenReady + ", " + playbackState);
                    if(playbackState == Player.STATE_ENDED) {
                        player.setPlayWhenReady(false);
                        player.seekTo(0);
                        isPlaying = false;
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
            // 传入和上次相同的信息时忽略
            if(lastId != null && lastId.equals(id)) {
                return;
            }
            this.init();
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
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
            lastId = id;
        }
        public void play(JSONObject value, String clientId) {
            LogUtil.i("play", clientId);
            if(value != null) {
                info(value);
            }
            isPlaying = true;
            player.setPlayWhenReady(true);
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
        }
        public void pause(String clientId) {
            LogUtil.i("pause", clientId);
            isPlaying = false;
            player.setPlayWhenReady(false);
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
        }
        public void stop(String clientId) {
            LogUtil.i("stop", clientId);
            isPlaying = false;
            player.stop();
            if(clientId != null && mainActivity != null) {
                JSONObject json = new JSONObject();
                json.put("id", lastId);
                mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
            }
        }
        public void release(String clientId) {
            LogUtil.i("release", clientId);
            lastId = null;
            percent = 0;
            isPlaying = false;
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
    }
    @Override
    public void onDestroy() {
        LogUtil.i("onDestroy");
        super.onDestroy();
        lastId = null;
        percent = 0;
        isPlaying = false;
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
    }
}
