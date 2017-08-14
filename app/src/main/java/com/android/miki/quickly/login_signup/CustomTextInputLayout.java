package com.android.miki.quickly.login_signup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

import com.android.miki.quickly.ui.EditTextUtil;

/**
 * Created by mpokr on 8/13/2017.
 */

public class CustomTextInputLayout extends TextInputLayout {

    private Drawable errorDrawable;

    public CustomTextInputLayout(Context context) {
        super(context);
    }

    public CustomTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
    }


    @Override
    public void setError(CharSequence error) {
        EditText editText = getEditText();
        if (error == null && editText != null) { // Revert to normal background.
            Drawable normalBackground = editText.getBackground();
            if (normalBackground != null) {
                editText.setBackground(normalBackground);
            }
        } else if (errorDrawable != null && editText != null) { // Set error background.
            editText.setBackground(errorDrawable);
        }
        super.setError(error);
    }

    public String getText() {
        return EditTextUtil.getText(getEditText());
    }

    public String getTrimmedText() {
        return EditTextUtil.getTrimmedText(getEditText());
    }
}
