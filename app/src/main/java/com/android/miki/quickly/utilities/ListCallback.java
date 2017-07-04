package com.android.miki.quickly.utilities;

import java.util.List;

/**
 * Created by mpokr on 7/4/2017.
 */

public interface ListCallback<T> {
    T done(List<T> data);
}
