package com.android.miki.quickly.core.network;

import android.content.Context;

/**
 * Created by Miki on 7/19/2017.
 */

public interface ConnectivityStatusObserver {

    void onConnect();
    void onDisconnect();
    Context retrieveContext();

}
