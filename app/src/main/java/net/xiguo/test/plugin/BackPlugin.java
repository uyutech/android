package net.xiguo.test.plugin;

import com.alibaba.fastjson.JSONObject;

import net.xiguo.test.X5Activity;
import net.xiguo.test.utils.LogUtil;

import org.xwalk.core.XWalkNavigationHistory;

/**
 * Created by army on 2017/3/27.
 */

public class BackPlugin extends H5Plugin {

    public BackPlugin(X5Activity activity) {
        super(activity);
    }

    @Override
    public void handle(JSONObject param) {
        String params = param.toJSONString();
        LogUtil.i("BackPlugin: " + params);
        JSONObject p = param.getJSONObject("param");
        if(p != null) {
            boolean prevent = p.getBoolean("prevent");
            if(!prevent) {
                boolean canGoBack = this.activity.getWebView().getNavigationHistory().canGoBack();
                LogUtil.i("BackPlugin: " + canGoBack);
                if(canGoBack) {
                    this.activity.getWebView().getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);//返回上一页面
//                    this.activity.getWebView().goBack();
                }
                else {
                    this.activity.finish();
                }
            }
        }
    }
}
