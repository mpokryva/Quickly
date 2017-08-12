package com.android.miki.quickly.ui;

import android.widget.EditText;

/**
 * Created by Miki on 8/9/2017.
 */

public class EditTextUtil {

    public static String getText(EditText editText) {
        if (editText == null) {
            return null;
        } else {
            return editText.getText().toString();
        }
    }

    public static String getTrimmedText(EditText editText) {
        String text = getText(editText);
        return (text == null) ? null : text.trim();
    }
}
