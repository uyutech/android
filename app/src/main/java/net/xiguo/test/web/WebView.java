package net.xiguo.test.web;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/6/12.
 */

public class WebView extends android.webkit.WebView {
    private SwipeRefreshLayout swipeRefreshLayout;
    private float startY;

    public WebView(Context context) {
        super(context);
    }
    public WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                if(getScrollY() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                }
                else {
                    swipeRefreshLayout.setEnabled(false);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(getScrollY() > 0 || event.getY() < startY) {
                    swipeRefreshLayout.setEnabled(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                swipeRefreshLayout.setEnabled(false);
                break;
        }
        return super.onTouchEvent(event);
    }
//    @Override
//    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
//        super.onScrollChanged(l, t, oldl, oldt);
////        LogUtil.i("onScrollChanged: ", this.getWebScrollY() + "");
//        if(getScrollY() == 0) {
//            swipeRefreshLayout.setEnabled(true);
//        }
//        else {
//            swipeRefreshLayout.setEnabled(false);
//        }
//    }

}
