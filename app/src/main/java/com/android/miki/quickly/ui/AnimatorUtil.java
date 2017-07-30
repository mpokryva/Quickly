package com.android.miki.quickly.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by Miki on 7/28/2017.
 */

public class AnimatorUtil {

    private View viewToAnimate;
    private Animation animation;
    private int duration;
    private Animation.AnimationListener listener;

    private AnimatorUtil(View viewToAnimate, Animation animation, int duration) {
        this.viewToAnimate = viewToAnimate;
        this.animation = animation;
        this.duration = duration;
    }

    private AnimatorUtil() {

    }

    public static Builder on(View view){
        Builder builder = new Builder();
        builder.on(view);
        return builder;
    }

    private void animate() {
        if (isReadyToAnimate()) {
            if (listener != null) {
                animation.setAnimationListener(listener);
            }
            animation.setDuration(duration);
            viewToAnimate.startAnimation(animation);
        }
    }

    private boolean isReadyToAnimate() {
        return viewToAnimate != null && animation != null;
    }

    public static final class Builder {

        private AnimatorUtil animatorUtil;

        private Builder() {
            animatorUtil = new AnimatorUtil();
        }

        public Builder with(Animation animation) {
            animatorUtil.animation = animation;
            return this;
        }

        public Builder on(View view) {
            animatorUtil.viewToAnimate = view;
            return this;
        }

        public Builder duration(int milliseconds) {
            animatorUtil.duration = milliseconds;
            return this;
        }

        public Builder listener(Animation.AnimationListener listener) {
            animatorUtil.listener = listener;
            return this;
        }

        public void animate() {
            animatorUtil.animate();
        }
    }





}
