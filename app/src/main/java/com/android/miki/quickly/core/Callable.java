package com.android.miki.quickly.core;

import com.android.miki.quickly.utilities.FirebaseListener;

/**
 * Represents objects that encapsulate an action (method).
 * For more info, look at the Command design pattern.
 * Created by Miki on 7/14/2017.
 */


public interface Callable<T> {
    void call(final FirebaseListener<T> listener);
}

