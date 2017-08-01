package net.xiguo.test.web;

import net.xiguo.test.BaseApplication;
import net.xiguo.test.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by army on 2017/4/28.
 */

public class LoadBridge {
    private static String h5Bridge = null;

    public static String getBridgeJs() {
        if(h5Bridge != null) {
            return h5Bridge;
        }
        InputStream is = null;
        try {
            is = BaseApplication.getContext().getAssets().open("h5_bridge.js");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            h5Bridge = sb.toString();
//            LogUtil.i("h5Bridge: " + h5Bridge);
        } catch (IOException e) {
            e.printStackTrace();
            h5Bridge = null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return h5Bridge;
    }
}
