package com.android.miki.quickly.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.android.miki.quickly.ui.EditTextUtil;

/**
 * Created by mpokr on 8/13/2017.
 */

public class CustomTextInputLayout extends TextInputLayout {

    private Drawable errorDrawable;
    private TextWatcher textWatcher;
    private Drawable defaultBackground;

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
        EditText editText = getEditText();
        if (textWatcher == null && editText != null) {
            defaultBackground = editText.getBackground();
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    setError(null);
                    setErrorEnabled(false);
                }
            };
            editText.addTextChangedListener(textWatcher);
        }
    }


    @Override
    public void setError(CharSequence error) {
        EditText editText = getEditText();
        if (error == null && editText != null) { // Revert to normal background.
            setErrorEnabled(false);
            if (defaultBackground != null) {
                editText.setBackground(defaultBackground);
            }
        } else if (errorDrawable != null && editText != null) { // Set error background.
            editText.setBackground(errorDrawable);
            setErrorEnabled(true);
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
