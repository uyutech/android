package cc.circling.web;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cc.circling.WebFragment;
import cc.circling.utils.LogUtil;

/**
 * Created by army on 2017/6/12.
 */

public class WebView extends android.webkit.WebView {
    private WebFragment webFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isStart = false;
    private float startY;
    private float startX;

    public WebView(Context context) {
        super(context);
    }
    public WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFragment(WebFragment webFragment) {
        this.webFragment = webFragment;
    }
    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.v("touch ", event.getAction() + ", " + event.getY() + ", " + getScrollY());
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(getScrollY() != 0) {
                    swipeRefreshLayout.setEnabled(false);
                }
                else {
                    isStart = true;
                    startX = event.getX();
                    startY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(getScrollY() > 0) {
                    swipeRefreshLayout.setEnabled(false);
                }
                else if(isStart) {
                    float diffY = event.getY() - startY;
                    float diffX = event.getX() - startX;
                    if(diffX != 0 && diffY != 0) {
                        isStart = false;
                        if(Math.abs(diffX) > Math.abs(diffY) || diffY < 0) {
                            swipeRefreshLayout.setEnabled(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isStart = false;
                swipeRefreshLayout.setEnabled(getScrollY() == 0);
                break;
        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        LogUtil.v("onScrollChanged: ", l + ", " + t + ", " + oldl + ", " + oldt);
        if(webFragment != null) {
            webFragment.onScrollChanged(t);
        }
    }

}
