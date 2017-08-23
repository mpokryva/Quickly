package com.android.miki.quickly.firebase_requests.chatroom_model_requests;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.firebase_requests.FirebaseRequest;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by mpokr on 8/20/2017.
 */


// T specifies the return type of the request.
public abstract class SingleChatRoomRequest<T> implements FirebaseRequest, Callable<T> {

    abstract Object value();

    @Override
    public void call(final FirebaseListener<T> listener) {
        ref().setValue(value(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    listener.onSuccess(null);
                } else {
                    listener.onError(FirebaseError.from(databaseError));
                }
            }
        });
    }

}
