package com.android.miki.quickly.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

/**
 * Created by Miki on 7/28/2017.
 */

public class ShrinkingAnimation extends ScaleAnimation {

    private final int DURATION = 200; // 500 ms
    private int visibilityOnFinish;

    public ShrinkingAnimation() {
        super(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        //this.setDuration(DURATION);
        setFillAfter(true);
    }

}
