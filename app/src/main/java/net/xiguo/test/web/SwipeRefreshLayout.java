package net.xiguo.test.web;

import android.content.Context;
import android.util.AttributeSet;

import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/7/16.
 */

public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {
    private boolean forced = false;

    public SwipeRefreshLayout(Context context) {
        super(context);
    }
    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(!forced) {
            super.setEnabled(enabled);
        }
    }
    public void setForceEnabled(boolean enabled) {
        LogUtil.i("setFoceEnabled: ", enabled + "");
        forced = !enabled;
        super.setEnabled(enabled);
    }
}
