package com.android.miki.quickly.core.network;

import android.util.Log;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.utils.GenericCallback;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Miki on 7/14/2017.
 */

public class FirebaseClient {

    public static FirebaseClient client;
    private static final String TAG = FirebaseClient.class.getName();
    private static int MAX_CONNECTION_TRIES = 4;


    /**
     * Issue a Firebase request. Do not call the listener's onLoading() method in the request.
     * This may cause problems.
     *
     * @param request  The Firebase request.
     * @param listener The callback listener that should receive and handle any results from the request.
     */
    public void execute(final Callable request, final FirebaseListener listener) {
        // Checking if there is connection to Firebase.
        listener.onLoading();
        int firstTry = 1;
        isConnected(new FirebaseListener<Void>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onError(FirebaseError error) {
                listener.onError(error);
            }

            @Override
            public void onSuccess(Void nothing) {
                // Working Internet connection.
                request.call(listener);
            }
        }, firstTry);

    }

    /**
     * Checks if has internet connection.
     * @param listener onError if no connection, onSuccess if there is.
     * @param tryNum How many times this method has already tried querying. Stops as MAX_CONNECTION_TRIES.
     */
    private void isConnected(final FirebaseListener<Void> listener, final int tryNum) {
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
                        listener.onSuccess(null);
                    } else {
                        Log.d(TAG, "Try number " + tryNum + " unsuccessful.");
                        isConnected(listener, tryNum + 1);
                    }
                }
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

    public static FirebaseClient getInstance() {
        if (client == null) {
            client = new FirebaseClient();
        }
        return client;
    }

}
