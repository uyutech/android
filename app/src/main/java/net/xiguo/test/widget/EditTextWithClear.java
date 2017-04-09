package net.xiguo.test.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/4/6.
 */

public class EditTextWithClear extends AppCompatEditText implements TextWatcher {
    private Drawable clearIcon;

    public EditTextWithClear(Context context) {
        this(context, null);
    }
    public EditTextWithClear(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }
    public EditTextWithClear(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        clearIcon = ContextCompat.getDrawable(context, R.drawable.clear);
        clearIcon.setBounds(0, 0, 36, 36);
        setClearIconVisible(getText().toString().length() > 0);
//        setCompoundDrawables(null, null, clearIcon, null);
//        setCompoundDrawablesWithIntrinsicBounds(null, null, clearIcon, null);
        setBackgroundResource(R.drawable.text_view);
        SpannableString ss = new SpannableString(getHint());
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(13, true);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setHint(new SpannedString(ss));
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        setClearIconVisible(s.length() > 0);
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void afterTextChanged(Editable s) {
    }
    private void setClearIconVisible(boolean visible) {
        setCompoundDrawables(null, null, visible ? clearIcon : null, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && getText().toString().length() > 0) {
            float x = event.getX();
            int clearXStart = getWidth() - getTotalPaddingEnd();
            int clearXEnd = getWidth() - getPaddingEnd();
            boolean touchClear = x >= clearXStart - 5 && x <= clearXEnd + 5;
            LogUtil.i("onTouchEvent: " + x + ", " + clearXStart + ", " + clearXEnd + ", " + touchClear);
            if (touchClear) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }
}
