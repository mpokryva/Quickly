package com.android.miki.quickly.firebase_requests.chatroom_model_requests;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.firebase_requests.FirebaseRequest;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mpokr on 8/20/2017.
 */

public abstract class ChatRoomRequestGroup<T> implements Callable<T> {

    private ChatRoom chatRoom;

    protected ChatRoomRequestGroup(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    @Override
    public void call(final FirebaseListener<T> listener) {
        for(Map.Entry<DatabaseReference, Object> entry : requests().entrySet()) {
            entry.getKey().setValue(entry.getValue(), new DatabaseReference.CompletionListener() {
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

    abstract LinkedHashMap<DatabaseReference, Object> requests();

    protected ChatRoom getChatRoom() {
        return this.chatRoom;
    }




}
