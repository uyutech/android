package cc.circling.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMs;
    private String path;

    public SingleMediaScanner(Context context, File file) {
        this(context, file.getAbsolutePath());
    }

    public SingleMediaScanner(Context context, String path) {
        this.path = path;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        LogUtil.i("onMediaScannerConnected", path);
        String mimeType = null;
        if(path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        }
        else if(path.endsWith(".png")) {
            mimeType = "image/png";
        }
        else if(path.endsWith(".gif")) {
            mimeType = "image/gif";
        }
        else if(path.endsWith(".mp3")) {
            mimeType = "audio/x-mpeg";
        }
        else if(path.endsWith(".mp4")) {
            mimeType = "video/mp4";
        }
        mMs.scanFile(path, mimeType);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        LogUtil.i("onScanCompleted", path);
        mMs.disconnect();
    }

}
