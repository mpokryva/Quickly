package com.android.miki.quickly.firebase_queries;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mpokr on 8/16/2017.
 */

public class ChangeGroupNameQuery extends FirebaseQuery implements Callable<Void> {


    /**
     * The chat room ID
     */
    private String roomId;
    private String newName;

    public ChangeGroupNameQuery(String roomId, String newName) {
        super((FirebaseDatabase.getInstance().getReference().child("availableChats")));
        this.roomId = this.roomId;
        this.newName = newName;
    }


    @Override
    public void call(final FirebaseListener<Void> listener) {
        getBaseRef().child(roomId).child("name").setValue(newName, new DatabaseReference.CompletionListener() {
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
