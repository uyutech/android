package cc.circling.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by army8735 on 2017/8/19.
 */

public class AndroidBug5497Workaround {
    public static void assistActivity(Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private FrameLayout content;
    private View mChildOfContent;
    private int usableHeightPrevious;
    private int usableHeightPrevious2;
    private FrameLayout.LayoutParams frameLayoutParams;
    private ViewGroup.LayoutParams layoutParams;
    private int contentHeight;
    private boolean isfirst = true;
    private Activity activity;
    private int statusBarHeight = 0;

    private AndroidBug5497Workaround(Activity activity) {
        //获取状态栏的高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        LogUtil.i("AndroidBug5497Workaround", resourceId + "");
        if(resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        this.activity = activity;
        content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                LogUtil.i("onGlobalLayout");
                resetLayoutByUsableHeight();
            }
        });

        //输入法界面出现变动都会调用这个监听事件
//        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                if (isfirst) {
//                    contentHeight = mChildOfContent.getHeight();//兼容华为等机型
//                    isfirst = false;
//                }
//                possiblyResizeChildOfContent();
//            }
//        });

        layoutParams = content.getLayoutParams();

//        frameLayoutParams = (FrameLayout.LayoutParams)
//                mChildOfContent.getLayoutParams();
    }

    //重新调整根布局的高度
    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        LogUtil.i("possiblyResizeChildOfContent", usableHeightNow + ", " + usableHeightPrevious2);

        //当前可见高度和上一次可见高度不一致 布局变动
        if (usableHeightNow != usableHeightPrevious) {
            //int usableHeightSansKeyboard2 = mChildOfContent.getHeight();//兼容华为等机型
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    //frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference + statusBarHeight;
                } else {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                }
            } else {
                frameLayoutParams.height = contentHeight;
            }

            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }
    /**     * 计算mChildOfContent可见高度     ** @return     */
    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }
    private void resetLayoutByUsableHeight() {
        int usableHeightNow = computeUsableHeight2();
        LogUtil.i("resetLayoutByUsableHeight", usableHeightNow + ", " + usableHeightPrevious2);
        //比较布局变化前后的View的可用高度
        if (usableHeightNow != usableHeightPrevious2) {
            //如果两次高度不一致
            //将当前的View的可用高度设置成View的实际高度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                layoutParams.height = usableHeightNow + statusBarHeight;
            }
            else {
                layoutParams.height = usableHeightNow;
            }//请求重新布局
            content.requestLayout();
            usableHeightPrevious2 = usableHeightNow;
        }
    }
    private int computeUsableHeight2() {
        LogUtil.i("computeUsableHeight2");
        Rect r = new Rect();
        content.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }
}
