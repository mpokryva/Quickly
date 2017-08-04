package com.android.miki.quickly.core.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.miki.quickly.utils.FirebaseError;

import java.util.HashSet;

/**
 * Created by Miki on 7/19/2017.
 */

public class ConnectivityStatusNotifier {

    private BroadcastReceiver receiver;
    private static ConnectivityStatusNotifier notifier;
    private static int MAX_CONNECTION_TRIES = 5;
    public static final String TAG = ConnectivityStatusNotifier.class.getName();
    private HashSet<ConnectivityStatusObserver> observerSet;

    public static ConnectivityStatusNotifier getInstance() {
        if (notifier == null) {
            notifier = new ConnectivityStatusNotifier();
        }
        return notifier;
    }

    private ConnectivityStatusNotifier() {
        observerSet = new HashSet<>();
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
                                        Thread.sleep(200);
                                        if (isConnected(context)) {
                                            notifyObservers(true);
                                            break;
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    notifyObservers(false);
                                }
                            }
                        }).run();
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
        Log.d(TAG, "Connected?: " + (activeNetwork != null && activeNetwork.isConnectedOrConnecting()));
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
    }

    public void registerObserver(ConnectivityStatusObserver observer) {
        observerSet.add(observer);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        observer.retrieveContext().registerReceiver(this.receiver, filter);
    }

    public void unregisterObserver(ConnectivityStatusObserver observer) {
        if (isObserverRegistered(observer)) {
            observer.retrieveContext().unregisterReceiver(receiver);
            observerSet.remove(observer);
        }
    }

    private void notifyObservers(boolean isConnected) {
        for (ConnectivityStatusObserver observer : observerSet) {
            if (isConnected) {
                observer.onConnect();
            } else {
                observer.onDisconnect(FirebaseError.serverError());
            }
        }
    }

    private boolean isObserverRegistered(ConnectivityStatusObserver observer) {
        return observerSet.contains(observer);
    }
}
