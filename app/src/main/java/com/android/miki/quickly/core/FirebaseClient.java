package com.android.miki.quickly.core;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utilities.FirebaseError;
import com.android.miki.quickly.utilities.FirebaseListener;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Created by Miki on 7/14/2017.
 */

public class FirebaseClient {

    private void makeFirebaseCall(final BiConsumer<HashMap<String, Object>, FirebaseListener<ChatRoom>> function, String param1, final FirebaseListener listener) {

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
            public void onSuccess(Boolean data) {
                if (data) { // Working Internet connection.
                    function.accept();
                } else {
                    listener.onError(FirebaseError.noFirebaseConnectionError());
                }
            }
        });

    }

}
