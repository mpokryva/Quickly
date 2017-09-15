package com.android.miki.quickly.utils;

import android.content.Context;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Miki on 9/1/2017.
 */

public class DialogBuilderHelper {

    public MaterialDialog.Builder inputDialog(Context context, int color) {
        return generalDialog(context, color)
                .inputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    public MaterialDialog.Builder generalDialog(Context context, int color) {
        return new MaterialDialog.Builder(context)
                .widgetColor(color)
                .positiveColor(color)
                .negativeColor(color);
    }
}
