package com.android.miki.quickly.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by Miki on 7/28/2017.
 */

public class ShrinkingImageButton extends AppCompatImageButton{

    public ShrinkingImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ShrinkingImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
