package com.android.miki.quickly.core.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Miki on 7/19/2017.
 */

public class ConnectivityStatusNotifier {

    private List<ConnectivityStatusObserver> observers;
    private BroadcastReceiver receiver;
    private static ConnectivityStatusNotifier notifier;
    private static int MAX_CONNECTION_TRIES = 5;
    public static final String TAG = ConnectivityStatusNotifier.class.getName();

    public static ConnectivityStatusNotifier getInstance() {
        if (notifier == null) {
            notifier = new ConnectivityStatusNotifier();
        }
        return notifier;
    }

    private ConnectivityStatusNotifier() {
        observers = new ArrayList<>();
        int i = 0;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                boolean isConnected = isConnected(context);
                boolean isConnectedOrConnecting = isConnectedOrConnecting(context);
                if (isConnectedOrConnecting) {
                    if (isConnected) {
                        notifyObservers(true); // Fully connected.
                    } else { // Network is connecting, but not yet connected.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int j = 0; j < MAX_CONNECTION_TRIES; j++) {
                                        Log.d(TAG, "Try number: " + j);
                                        if (isConnected(context)) {
                                            notifyObservers(true);
                                        }
                                        Thread.sleep(500);
                                    }
                                    notifyObservers(false);
                                } catch (InterruptedException e) {
                                    notifyObservers(false);
                                }
                            }
                        });
                    }
                } else { // If not even in the process of connecting, then network was just lost.
                    notifyObservers(false);
                }
            }
        };
    }

    private boolean isConnectedOrConnecting(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
    }

    public void registerObserver(ConnectivityStatusObserver observer) {
        observers.add(observer);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        observer.retrieveContext().registerReceiver(this.receiver, filter);
    }

    public void unregisterObserver(ConnectivityStatusObserver observer) {
        observer.retrieveContext().unregisterReceiver(receiver);
        observers.remove(observer);
    }

    private void notifyObservers(boolean isConnected) {
        for (ConnectivityStatusObserver observer : observers) {
            if (isConnected) {
                observer.onConnect();
            } else {
                observer.onDisconnect();
            }
        }
    }
}
