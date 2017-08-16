package com.android.miki.quickly.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.Button;

import com.android.miki.quickly.R;

/**
 * Created by mpokr on 8/14/2017.
 */

public class FBLoginButton extends AppCompatButton {


    public FBLoginButton(Context context) {
        super(context);
    }

    public FBLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Drawable background = ContextCompat.getDrawable(context, R.drawable.fb_login_button_background);
        setBackground(background);
        String fbButtonText = getResources().getString(R.string.continue_with_facebook);
        SpannableStringBuilder builder = new SpannableStringBuilder(fbButtonText);
        int boldStartIndex = 0;
        int boldEndIndex = 8;
        builder.setSpan(new StyleSpan(Typeface.BOLD), boldStartIndex, boldEndIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        boldStartIndex = 14;
        boldEndIndex = fbButtonText.length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), boldStartIndex, boldEndIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        this.setText(builder);
    }

    public FBLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



}
