package cc.circling.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by army8735 on 2018/1/17.
 */

public class ImgUtil {
    public static Bitmap parseBase64(String img) {
        try {
            byte[] ba = Base64.decode(img, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
