package com.android.miki.quickly.utilities;

import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public interface Callback<T> {
    T done(T data);
}
