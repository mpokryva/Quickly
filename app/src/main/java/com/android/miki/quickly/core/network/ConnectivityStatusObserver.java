package com.android.miki.quickly.core.network;

import android.content.Context;

import com.android.miki.quickly.utils.FirebaseError;

/**
 * Created by Miki on 7/19/2017.
 */

public interface ConnectivityStatusObserver {

    void onConnect();
    void onDisconnect(FirebaseError error);
    Context retrieveContext();

}
