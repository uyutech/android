package cc.circling;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2018/1/15.
 */

public class MediaService extends Service {
    private PlayBinder playBinder = new PlayBinder();
    private X5Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared;
    private boolean autoStart;
    private Timer timer;

    class PlayBinder extends Binder {
        public void start(final X5Activity activity) {
            LogUtil.i("start");
            MediaService.this.activity = activity;
            if(mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        LogUtil.i("onPrepared", mediaPlayer.getDuration() + "");
                        isPrepared = true;
                        if(autoStart) {
                            mediaPlayer.start();
                            autoStart = false;
                        }
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('prepared'," + mediaPlayer.getDuration() + ")");
                                }
                            });
                        }
                    }
                });
                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, final int percent) {
                        LogUtil.i("onBufferingUpdate", percent + "");
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('progress', " + percent + ");");
                                }
                            });
                        }
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        LogUtil.i("onCompletion");
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('ended')");
                                }
                            });
                        }
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        LogUtil.i("onError");
                        return false;
                    }
                });
            }
            if(timer == null) {
                timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(mediaPlayer.isPlaying()) {
                            LogUtil.i("timer", mediaPlayer.getCurrentPosition()
                                    + ", " + mediaPlayer.getDuration());
                            if(activity != null) {
                                final JSONObject json = new JSONObject();
                                json.put("currentTime", mediaPlayer.getCurrentPosition());
                                json.put("duration", mediaPlayer.getDuration());
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.getWebView().loadUrl("javascript: window.ZhuanQuanJSBridge && ZhuanQuanJSBridge.emit('timeupdate', '" + json.toJSONString() + "');");
                                    }
                                });
                            }
                        }
                    }
                };
                timer.schedule(timerTask, 0, 200);
            }
        }
        public void setUrl(String url) {
            LogUtil.i("setUrl", url);
            if(url == null || url.equals("")) {
                return;
            }
            mediaPlayer.reset();
            isPrepared = false;
            autoStart = false;
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void play() {
            LogUtil.i("play", isPrepared + "");
            if(isPrepared) {
                mediaPlayer.start();
            }
            else {
                autoStart = true;
            }
        }
        public void pause() {
            LogUtil.i("pause");
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
        public void stop() {
            LogUtil.i("stop");
            mediaPlayer.stop();
            mediaPlayer.release();
            isPrepared = false;
            autoStart = false;
        }
        public void reset(String url) {
            LogUtil.i("reset");
            stop();
            setUrl(url);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return playBinder;
    }
    @Override public void onCreate() {
        super.onCreate();
        LogUtil.i("onCreate");
    }
    @Override public void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy");
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        isPrepared = false;
        autoStart = false;
        timer.cancel();
        timer = null;
    }
}
