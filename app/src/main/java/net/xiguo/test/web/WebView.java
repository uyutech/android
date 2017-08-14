package net.xiguo.test.web;

import android.content.Context;
import android.util.AttributeSet;

import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/6/12.
 */

public class WebView extends android.webkit.WebView {
    private SwipeRefreshLayout swipeRefreshLayout;

    public WebView(Context context) {
        this(context, null);
    }
    public WebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public WebView(Context context, AttributeSet attrs, int var) {
        super(context, attrs, var);
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        LogUtil.i("onScrollChanged: ", this.getWebScrollY() + "");
        if(getScrollY() == 0) {
            swipeRefreshLayout.setEnabled(true);
        }
        else {
            swipeRefreshLayout.setEnabled(false);
        }
    }

}
