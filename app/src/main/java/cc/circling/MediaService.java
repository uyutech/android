package cc.circling;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cc.circling.utils.LogUtil;
import cc.circling.utils.MediaUrl2IDCache;
//import tv.danmaku.ijk.media.player.IMediaPlayer;
//import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by army8735 on 2018/1/15.
 */

public class MediaService extends Service {
    private PlayBinder playBinder = new PlayBinder();
    private X5Activity activity;
    private MediaPlayer mediaPlayer;
//    private IjkMediaPlayer mediaPlayer;
    private boolean prepareAsync;
    private boolean prepared;
    private boolean autoStart;
    private int duration;
    private String lastUrl;
    private Timer timer;
    private TimerTask timerTask;
    private CacheListener cacheListener;

    class PlayBinder extends Binder {
        private int percentsAvailable;

        public void start(final X5Activity activity) {
            LogUtil.i("start");
            MediaService.this.activity = activity;
        }
        private void init() {
            this.stop();
            this.percentsAvailable = 0;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    LogUtil.i("onPrepared", mediaPlayer.getDuration() + "");
                    prepared = true;
                    if(autoStart) {
                        if(!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                        autoStart = false;
                    }
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(activity == null || activity.getWebView() == null) {
                                    return;
                                }
                                final JSONObject json = new JSONObject();
                                duration = Math.max(0, mediaPlayer.getDuration());
                                json.put("duration", duration);
                                activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('mediaPrepared','" + json.toJSONString() + "')");
                            }
                        });
                    }
                }
            });
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, final int percent) {
                    LogUtil.i("onBufferingUpdate", percent + ", " + percentsAvailable);
                    // 偶现网络读取尚未到100时media加载直接跳100，屏蔽之
                    if(percent == 100 && percentsAvailable != 100) {
                        return;
                    }
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(activity == null || activity.getWebView() == null) {
                                    return;
                                }
                                JSONObject json = new JSONObject();
                                duration = Math.max(0, mediaPlayer.getDuration());
                                json.put("duration", duration);
                                json.put("percent", percent);
                                json.put("prepared", prepared);
                                activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('mediaProgress', '" + json.toJSONString() + "');");
                            }
                        });
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    LogUtil.i("onCompletion");
                    // TODO: 加载出错时，在onError之后也会触发
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(activity == null || activity.getWebView() == null) {
                                    return;
                                }
                                JSONObject json = new JSONObject();
                                json.put("currentTime", mediaPlayer.getCurrentPosition());
                                json.put("duration", duration);
                                activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('mediaEnd', '" + json.toJSONString() + "')");
                            }
                        });
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtil.i("onError", what + ", " + extra);
                    return false;
                }
            });
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogUtil.i("onInfo", what + ", " + extra);
                    return false;
                }
            });
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(activity == null || activity.getWebView() == null) {
                                        return;
                                    }
                                    final JSONObject json = new JSONObject();
                                    json.put("currentTime", mediaPlayer.getCurrentPosition());
                                    duration = Math.max(0, mediaPlayer.getDuration());
                                    json.put("duration", duration);
                                    activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('mediaTimeupdate', '" + json.toJSONString() + "');");
                                }
                            });
                        }
                    }
                }
            };
            timer.schedule(timerTask, 0, 200);
        }
        public void info(JSONObject value, final String clientId) {
            LogUtil.i("info", value.toJSONString());
            String url = value.getString("url");
            String name = value.getString("name");
            if(name != null && name.length() > 0) {
                MediaUrl2IDCache.put(url, name);
            }
            if(url == null || url.equals("")) {
                return;
            }
            LogUtil.i("info", url + ", " + lastUrl);

            HttpProxyCacheServer proxy = BaseApplication.getProxy();
            final boolean isCached = proxy.isCached(url);
            if(url.equals(lastUrl)) {
                if(clientId != null && activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(activity == null || activity.getWebView() == null) {
                                return;
                            }
                            JSONObject json = new JSONObject();
                            json.put("same", true);
                            json.put("isCached", isCached);
                            activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "', '" + json.toJSONString() + "');");
                        }
                    });
                }
                return;
            }
            this.init();
            lastUrl = url;

            LogUtil.i("isCached", isCached + "");
            if(isCached) {
                if(activity != null && clientId != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(activity == null || activity.getWebView() == null) {
                                return;
                            }
                            JSONObject json = new JSONObject();
                            json.put("isCached", isCached);
                            activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge._invokeJS('" + clientId + "', '" + json.toJSONString() + "');");
                        }
                    });
                }
            }
            else {
                if(cacheListener != null) {
                    proxy.unregisterCacheListener(cacheListener);
                }
                cacheListener = new CacheListener() {
                    @Override
                    public void onCacheAvailable(File cacheFile, String url, final int percentsAvailable) {
                        LogUtil.i("onCacheAvailable", percentsAvailable + "");
                        PlayBinder.this.percentsAvailable = percentsAvailable;
                    }
                };
                proxy.registerCacheListener(cacheListener, url);
            }
            url = proxy.getProxyUrl(url);
            try {
                mediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
                lastUrl = null;
            }
            if(clientId != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(activity == null || activity.getWebView() == null) {
                            return;
                        }
                        JSONObject json = new JSONObject();
                        json.put("isCached", isCached);
                        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "', '" + json.toJSONString() + "');");
                    }
                });
            }
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
            if(clientId != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(activity == null || activity.getWebView() == null) {
                            return;
                        }
                        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
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
            if(clientId != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(activity == null || activity.getWebView() == null) {
                            return;
                        }
                        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
                    }
                });
            }
        }
        public void stop() {
            this.stop(null);
        }
        public void stop(final String clientId) {
            LogUtil.i("stop");
            if(mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            prepareAsync = false;
            prepared = false;
            autoStart = false;
            lastUrl = null;
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
            if(clientId != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(activity == null || activity.getWebView() == null) {
                            return;
                        }
                        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
                    }
                });
            }
        }
        public void seek(JSONObject value, final String clientId) {
            int time = value.getInteger("time");
            LogUtil.i("seek", time + "");
            mediaPlayer.seekTo(time);
            if(clientId != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(activity == null || activity.getWebView() == null) {
                            return;
                        }
                        activity.getWebView().loadUrl("javascript: ZhuanQuanJSBridge._invokeJS('" + clientId + "');");
                    }
                });
            }
        }
        public void end(X5Activity activity) {
            LogUtil.i("end");
            if(MediaService.this.activity != null && MediaService.this.activity == activity) {
                LogUtil.i("end in");
                MediaService.this.activity = null;
            }
//            if(timer != null) {
//                timer.cancel();
//                timer = null;
//            }
//            if(timerTask != null) {
//                timerTask.cancel();
//                timerTask = null;
//            }
//            if(cacheListener != null) {
//                BaseApplication.getProxy().unregisterCacheListener(cacheListener);
//                cacheListener = null;
//            }
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
        lastUrl = null;
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
