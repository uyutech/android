package cc.circling.plugin;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.io.File;

import cc.circling.BaseApplication;
import cc.circling.BuildConfig;
import cc.circling.R;
import cc.circling.X5Activity;
import cc.circling.utils.LogUtil;

/**
 * Created by army8735 on 2017/12/8.
 */

public class DownloadPlugin extends H5Plugin {
    public static int downloadID = 0;

    public DownloadPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject json) {
        LogUtil.i("DownloadPlugin: " + json.toJSONString());
        JSONObject param = json.getJSONObject("param");
        if(param != null) {
            String url = param.getString("url");
            String name = param.getString("name");
            if(url != null && !url.equals("")) {
                if(name == null || name.equals("")) {
                    name = url;
                }
                final String fileName = name;
                int permissionRead = ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                int permissionWrite = ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                LogUtil.i("permissionRead", permissionRead + "");
                LogUtil.i("permissionWrite", permissionWrite + "");
                if(permissionRead != PackageManager.PERMISSION_GRANTED || permissionWrite != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
                    return;
                }

                final String type;
                if(fileName.endsWith(".mp3")) {
                    type = "audio/*";
                }
                else if(fileName.endsWith(".mp4")) {
                    type = "video/*";
                }
                else {
                    type = "image/*";
                }

                // 创建目录
                String directoryPath = "";
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    LogUtil.i("DownloadPlugin: MEDIA_MOUNTED");
                    if(fileName.endsWith(".mp3")) {
                        directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    }
                    else if(fileName.endsWith(".mp4")) {
                        directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
                    }
                    else {
                        directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                    }
                }
                else {
                    LogUtil.i("DownloadPlugin: !MEDIA_MOUNTED");
                    directoryPath = BaseApplication.getContext().getFilesDir().getAbsolutePath();
                }
                directoryPath += File.separator + "circling";
                File file = new File(directoryPath);
                if(!file.exists()) {
                    file.mkdirs();
                }
                LogUtil.i("directoryPath: " + directoryPath);
                FileDownloadHelper.holdContext(BaseApplication.getContext());
                final String path = directoryPath + File.separator + fileName;
                LogUtil.i("path: " + path);
                File downloadFile = new File(path);

                final int currentID = downloadID++;
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, "download");
                builder.setWhen(System.currentTimeMillis());
                builder.setTicker("准备下载 " + fileName);
                builder.setContentTitle("准备下载 " + fileName);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher));

                Intent intent = new Intent(Intent.ACTION_VIEW);
                final Uri uri;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(BaseApplication.getContext(),
                            BuildConfig.APPLICATION_ID + ".download", downloadFile);
                    LogUtil.i("uri: " + uri);
                    intent.setDataAndType(uri, type);
                }
                else {
                    uri = Uri.fromFile(downloadFile);
                    LogUtil.i("uri2: " + uri);
                    intent.setDataAndType(uri, type);
                }
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                final PendingIntent pIntent = PendingIntent.getActivity(activity, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//                builder.setContentIntent(pIntent);
                Notification notification = builder.build();
                final NotificationManager notificationManager =
                        (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(currentID, notification);

                FileDownloader.getImpl().create(url)
                    .setPath(path)
                    .setListener(new FileDownloadListener() {
                        @Override
                        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                            LogUtil.i("pending", soFarBytes + ", " + totalBytes);
                        }

                        @Override
                        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                            builder.setTicker("开始下载 " + fileName);
                            builder.setContentTitle("开始下载 " + fileName);
                            builder.setContentText("进度 0%");
                        }

                        @Override
                        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                            int progress = (int) (soFarBytes * 1.0f / totalBytes * 100);
                            builder.setTicker("正在下载 " + fileName);
                            builder.setContentTitle("正在下载 " + fileName);
                            builder.setContentText("进度 " + progress + "%");
                            builder.setProgress(totalBytes, soFarBytes, false);
                            notificationManager.notify(currentID, builder.build());
                        }

                        @Override
                        protected void blockComplete(BaseDownloadTask task) {
                            builder.setTicker("下载完成 " + fileName);
                            builder.setContentTitle("下载完成 " + fileName);
                            builder.setContentText("下载完成");
                            builder.setProgress(0, 0, false);
                            notificationManager.notify(currentID, builder.build());
                        }

                        @Override
                        protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                            LogUtil.i("retry");
                        }

                        @Override
                        protected void completed(BaseDownloadTask task) {
                            builder.setTicker("下载完成 " + fileName);
                            builder.setContentTitle("下载完成 " + fileName);
                            builder.setContentText("下载完成");
                            builder.setProgress(0, 0, false);
                            builder.setContentIntent(pIntent);
                            notificationManager.notify(currentID, builder.build());
                            // 通知媒体库更新，可以在相册等看到
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(uri);
                            LogUtil.i("completed: ", intent.toString());
                            activity.sendBroadcast(intent);
                        }

                        @Override
                        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                            builder.setTicker("下载暂停 " + fileName);
                            builder.setContentTitle("下载完成 " + fileName);
                            builder.setContentText("下载暂停");
                            builder.setProgress(totalBytes, soFarBytes, false);
                        }

                        @Override
                        protected void error(BaseDownloadTask task, Throwable e) {
                            builder.setTicker("下载错误 " + fileName);
                            builder.setContentTitle("下载错误 " + fileName);
                            builder.setContentText("下载错误");
                            builder.setProgress(1, 0, false);
                        }

                        @Override
                        protected void warn(BaseDownloadTask task) {
                            LogUtil.i("warn");
                        }
                    }).start();
            }
        }
    }
}
