package com.android.miki.quickly.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.android.miki.quickly.R;

/**
 * Created by mpokr on 8/26/2017.
 */

public class CustomProgressWheel extends ProgressBar {

    private static final int DEFAULT_COLOR_RES_ID = R.color.LightBlue;

    public CustomProgressWheel(Context context) {
        super(context);
        configure(DEFAULT_COLOR_RES_ID);
    }

    public CustomProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure(DEFAULT_COLOR_RES_ID);
    }

    public CustomProgressWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure(DEFAULT_COLOR_RES_ID);
    }

    public CustomProgressWheel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        configure(DEFAULT_COLOR_RES_ID);
    }

    private void configure(int colorResId) {
        this.setIndeterminate(true);
        this.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), colorResId), PorterDuff.Mode.MULTIPLY);
    }
}
