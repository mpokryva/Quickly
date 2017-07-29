package com.android.miki.quickly.core.network;

import com.android.miki.quickly.core.Callable;
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


    /**
     * Issue a Firebase query. Do not call the listener's onLoading() method in the query.
     * This may cause problems.
     * @param query The Firebase query.
     * @param listener The callback listener that should receive and handle any results from the query.
     */
    public void queryFirebase(final Callable query, final FirebaseListener listener) {
        //dataGenerator.deleteAllData();
        //dataGenerator.createTestChats(30);

        // Checking if there is connection to Firebase.
        isConnected(new FirebaseListener<Boolean>() {
            @Override
            public void onLoading() {
                listener.onLoading();
            }

            @Override
            public void onError(FirebaseError error) {
                listener.onError(error);
            }

            @Override
            public void onSuccess(Boolean isConnectedToDatabase) {
                if (isConnectedToDatabase) { // Working Internet connection.
                    query.call(listener);
                } else {
                    listener.onError(FirebaseError.noFirebaseConnection());
                }
            }
        });

    }

    private void isConnected(final FirebaseListener<Boolean> listener) {
        listener.onLoading();
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isConnected = dataSnapshot.getValue(Boolean.class);
                listener.onSuccess(isConnected);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(new FirebaseError((databaseError)));
            }
        });
    }

    public static FirebaseClient getInstance() {
        if (client == null) {
            client = new FirebaseClient();
        }
        return client;
    }

}
