package cc.circling.web;

import android.content.Context;
import android.util.AttributeSet;

import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/7/16.
 */

public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {
    private boolean canEnabled = true;

    public SwipeRefreshLayout(Context context) {
        super(context);
    }
    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
//        LogUtil.i("setEnabled: ", enabled + ", " + canEnabled);
        if(canEnabled) {
            super.setEnabled(enabled);
        }
    }
    public void setCanEnabled(boolean enabled) {
        LogUtil.i("setCanEnabled: ", enabled + "");
        canEnabled = enabled;
        if(!enabled) {
            super.setEnabled(false);
        }
    }
}
