package cc.circling;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.alibaba.fastjson.JSONObject;
import com.danikula.videocache.CacheListener;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
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
    private MediaPlayer mediaPlayer;
    private SimpleExoPlayer player;
    private boolean prepareAsync;
    private boolean prepared;
    private boolean autoStart;
    private int duration;
    private int percent;
    private String lastId;
    private Timer timer;
    private TimerTask timerTask;
    private CacheListener cacheListener;

    class PlayBinder extends Binder {
        private int percentsAvailable;

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
//            this.release();
//            this.percentsAvailable = 0;
//
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    LogUtil.i("onPrepared", mediaPlayer.getDuration() + "");
//                    prepared = true;
//                    if(autoStart) {
//                        if(!mediaPlayer.isPlaying()) {
//                            mediaPlayer.start();
//                        }
//                        autoStart = false;
//                    }
//                    if(mainActivity != null) {
//                        mainActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(mainActivity == null) {
//                                    return;
//                                }
//                                final JSONObject json = new JSONObject();
//                                json.put("id", lastId);
//                                duration = Math.max(0, mediaPlayer.getDuration());
//                                json.put("duration", duration);
//                                mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaPrepared'," + json.toJSONString() + ");");
//                            }
//                        });
//                    }
//                }
//            });
//            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
//                @Override
//                public void onBufferingUpdate(MediaPlayer mp, final int percent) {
//                    LogUtil.i("onBufferingUpdate", percent + ", " + percentsAvailable);
//                    // 偶现网络读取尚未到100时media加载直接跳100，屏蔽之
//                    if(percent == 100 && percentsAvailable != 100) {
//                        return;
//                    }
//                    MediaService.this.percent = percent;
//                    if(mainActivity != null) {
//                        mainActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(mainActivity == null) {
//                                    return;
//                                }
//                                JSONObject json = new JSONObject();
//                                json.put("id", lastId);
//                                duration = Math.max(0, mediaPlayer.getDuration());
//                                json.put("duration", duration);
//                                json.put("percent", percent);
//                                json.put("prepared", prepared);
//                                mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaProgress', " + json.toJSONString() + ");");
//                            }
//                        });
//                    }
//                }
//            });
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    LogUtil.i("onCompletion", (mainActivity != null) + ", " + (percent == 100));
//                    // percent为100加载完毕之后才能触发播放结束，避免proxycache网络错误造成加载过程中出现结束事件
//                    if(mainActivity != null && percent == 100) {
//                        mainActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(mainActivity == null) {
//                                    return;
//                                }
//                                JSONObject json = new JSONObject();
//                                json.put("id", lastId);
//                                json.put("currentTime", mediaPlayer.getCurrentPosition());
//                                json.put("duration", duration);
//                                mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaEnd', " + json.toJSONString() + ");");
//                            }
//                        });
//                    }
//                }
//            });
//            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                @Override
//                public boolean onError(MediaPlayer mp, int what, int extra) {
//                    LogUtil.i("onError", what + ", " + extra);
//                    return false;
//                }
//            });
//            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//                @Override
//                public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                    LogUtil.i("onInfo", what + ", " + extra);
//                    return false;
//                }
//            });
//            timer = new Timer();
//            timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    if(mainActivity != null && mediaPlayer != null && mediaPlayer.isPlaying()) {
//                        mainActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(mainActivity == null || !mediaPlayer.isPlaying()) {
//                                    return;
//                                }
//                                final JSONObject json = new JSONObject();
//                                json.put("id", lastId);
//                                json.put("currentTime", mediaPlayer.getCurrentPosition());
//                                duration = Math.max(0, mediaPlayer.getDuration());
//                                json.put("duration", duration);
//                                json.put("percent", percent);
//                                json.put("prepared", prepared);
//                                mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge.emit('mediaTimeupdate', " + json.toJSONString() + ");");
//                            }
//                        });
//                    }
//                }
//            };
//            timer.schedule(timerTask, 0, 200);
        }
        public void info(JSONObject value, final String clientId) {
            LogUtil.i("info", value.toJSONString());
            this.init();
            String url = value.getString("url");
            String name = value.getString("name");
            String id = value.getString("id");
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
            player.setPlayWhenReady(true);
            // url对应name作为本地缓存文件名
//            if(name != null && name.length() > 0) {
//                MediaUrl2IDCache.put(url, name);
//            }
//            if(url == null || url.equals("")) {
//                return;
//            }
//            LogUtil.i("info", url + ", " + name + ", " + (id == null ? "null" : id) + ", " + (lastId == null ? "null" : lastId));
//
//            HttpProxyCacheServer proxy = BaseApplication.getProxy();
//            final boolean isCached = proxy.isCached(url);
//            if(id != null && id.equals(lastId) && clientId != null && mainActivity != null) {
//                mainActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(mainActivity == null) {
//                            return;
//                        }
//                        LogUtil.i("info same");
//                        JSONObject json = new JSONObject();
//                        json.put("id", lastId);
//                        json.put("same", true);
//                        json.put("isCached", isCached);
//                        json.put("duration", duration);
//                        json.put("percent", percent);
//                        json.put("prepared", prepared);
//                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
//                    }
//                });
//                return;
//            }
//            this.init();
//            lastId = id;
//
//            LogUtil.i("isCached", isCached + "");
//            if(isCached) {
//                percent = 100;
//                if(mainActivity != null && clientId != null) {
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(mainActivity == null) {
//                                return;
//                            }
//                            JSONObject json = new JSONObject();
//                            json.put("id", lastId);
//                            json.put("isCached", isCached);
//                            mainActivity.evaluateJavascript("window.ZhuanQuanJsBridge && ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
//                        }
//                    });
//                }
//            }
//            else {
//                if(cacheListener != null) {
//                    proxy.unregisterCacheListener(cacheListener);
//                }
//                cacheListener = new CacheListener() {
//                    @Override
//                    public void onCacheAvailable(File cacheFile, String url, final int percentsAvailable) {
//                        LogUtil.i("onCacheAvailable", percentsAvailable + "");
//                        PlayBinder.this.percentsAvailable = percentsAvailable;
//                    }
//                };
//                proxy.registerCacheListener(cacheListener, url);
//            }
//            url = proxy.getProxyUrl(url);
//            try {
//                mediaPlayer.setDataSource(url);
//            } catch (IOException e) {
//                e.printStackTrace();
//                lastId = null;
//            }
//            if(clientId != null && mainActivity != null) {
//                mainActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(mainActivity == null) {
//                            return;
//                        }
//                        JSONObject json = new JSONObject();
//                        json.put("id", lastId);
//                        json.put("isCached", isCached);
//                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
//                    }
//                });
//            }
        }
        public void play(final String clientId) {
            LogUtil.i("play", prepareAsync + ", " + prepared);
            // 媒体流已缓冲准备完毕可以播放
            if(prepared) {
                if(!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
            // 异步请求加载发出后且尚未准备完成时标明加载完毕后自动播放
            else if(prepareAsync) {
                autoStart = true;
            }
            // 进行媒体流异步请求加载
            else {
                mediaPlayer.prepareAsync();
                prepareAsync = true;
                autoStart = true;
            }
            if(clientId != null && mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mainActivity == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        public void pause(final String clientId) {
            LogUtil.i("pause");
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            autoStart = false;
            if(clientId != null && mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mainActivity == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        public void stop(final String clientId) {
            LogUtil.i("stop");
            if(mediaPlayer != null) {
                mediaPlayer.stop();
            }
            if(clientId != null && mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mainActivity == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        public void release() {
            this.release(null);
        }
        public void release(final String clientId) {
            LogUtil.i("release", (clientId == null) + "");
            if(mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            prepareAsync = false;
            prepared = false;
            autoStart = false;
            lastId = null;
            percent = 0;
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
            if(timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            if(cacheListener != null) {
                BaseApplication.getProxy().unregisterCacheListener(cacheListener);
                cacheListener = null;
            }
            if(clientId != null && mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mainActivity == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        public void seek(JSONObject value, final String clientId) {
            int time = value.getInteger("time");
            LogUtil.i("seek", time + "");
            mediaPlayer.seekTo(time);
            if(clientId != null && mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mainActivity == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("id", lastId);
                        mainActivity.evaluateJavascript("ZhuanQuanJsBridge._invokeJS('" + clientId + "', " + json.toJSONString() + ");");
                    }
                });
            }
        }
        public void end(MainActivity mainActivity) {
            LogUtil.i("end");
            if(MediaService.this.mainActivity != null && MediaService.this.mainActivity == mainActivity) {
                LogUtil.i("end in");
                MediaService.this.mainActivity = null;
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
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        prepareAsync = false;
        prepared = false;
        autoStart = false;
        lastId = null;
        percent = 0;
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if(cacheListener != null) {
            BaseApplication.getProxy().unregisterCacheListener(cacheListener);
            cacheListener = null;
        }
    }
}
