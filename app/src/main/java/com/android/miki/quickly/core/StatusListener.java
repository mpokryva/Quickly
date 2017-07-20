package com.android.miki.quickly.core;

/**
 * Created by Miki on 7/6/2017.
 */

public interface StatusListener<T> {
    void onLoading();
    void onError();
    void onSuccess(T data);
}
