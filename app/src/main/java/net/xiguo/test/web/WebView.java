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

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        LogUtil.i("touch ", event.getAction() + ", " + event.getY() + ", " + getScrollY());
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
//                    LogUtil.i("3setEnabled(false)");
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
//                LogUtil.i("4setEnabled(" + getScrollY() + ")");
                swipeRefreshLayout.setEnabled(getScrollY() == 0);
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
