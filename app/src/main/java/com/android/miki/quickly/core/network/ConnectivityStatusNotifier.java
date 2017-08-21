package com.android.miki.quickly.core.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.android.miki.quickly.utils.GenericCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

/**
 * Created by Miki on 7/19/2017.
 */

public class ConnectivityStatusNotifier {

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
        listenForConnectionChanges();
    }


    public void registerObserver(ConnectivityStatusObserver observer) {
        observerSet.add(observer);
    }

    public void unregisterObserver(ConnectivityStatusObserver observer) {
        observerSet.remove(observer);
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

    private void isConnected(final FirebaseListener<Boolean> listener, final int tryNum) {
        if (tryNum > MAX_CONNECTION_TRIES) {
            listener.onError(FirebaseError.serverError());
        }
        queryConnection(new GenericCallback<Boolean>() {
            @Override
            public void onFinished(Boolean isConnected) {
                if (isConnected == null) {
                    listener.onError(FirebaseError.serverError());
                } else {
                    if (isConnected) {
                        listener.onSuccess(true);
                    } else {
                        Log.d(TAG, "Try number " + tryNum + " unsuccessful.");
                        isConnected(listener, tryNum + 1);
                    }
                }
            }
        });
    }

    private void listenForConnectionChanges() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isConnected = dataSnapshot.getValue(Boolean.class);
                notifyObservers(isConnected);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void queryConnection(final GenericCallback<Boolean> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                    connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean isConnected = dataSnapshot.getValue(Boolean.class);
                            Log.d(TAG, "Client isConnected?: " + isConnected);
                            callback.onFinished(isConnected);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onFinished(false);

                        }
                    });
                } catch (InterruptedException e) {
                    callback.onFinished(false);
                }
            }
        }).run();
    }
}
