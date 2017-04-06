package net.xiguo.test.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import net.xiguo.test.R;

/**
 * Created by army on 2017/4/6.
 */

public class EditTextWithClear extends AppCompatEditText {
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
        clearIcon.setBounds(0, 0, 30, 30);
        setCompoundDrawables(null, null, clearIcon, null);
//        setCompoundDrawablesWithIntrinsicBounds(null, null, clearIcon, null);
        setBackgroundResource(R.drawable.text_view);
    }
}
