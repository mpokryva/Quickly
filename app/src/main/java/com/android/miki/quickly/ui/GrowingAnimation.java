package com.android.miki.quickly.ui;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by Miki on 7/29/2017.
 */

public class GrowingAnimation extends ScaleAnimation {

    private final int DURATION = 200; // 500 ms

    public GrowingAnimation(AnimationListener listener) {
        super(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        this.setDuration(DURATION);
        this.setAnimationListener(listener);
    }

}
