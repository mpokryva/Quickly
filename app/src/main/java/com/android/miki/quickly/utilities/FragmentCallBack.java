package com.android.miki.quickly.utilities;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by mpokr on 7/4/2017.
 */

public interface FragmentCallBack<T> {
    View done(T data);
}
