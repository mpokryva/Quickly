package com.android.miki.quickly.firebase_queries;

import android.support.annotation.NonNull;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utilities.FirebaseError;
import com.android.miki.quickly.utilities.FirebaseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Miki on 7/14/2017.
 */

public class ChatRoomQuery extends FirebaseQuery implements Callable {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    /**
     * The ID of the chat room to retrieve.
     */
    private String chatId;
    private FirebaseListener<ChatRoom> listener;

    public ChatRoomQuery(String chatId, FirebaseListener<ChatRoom> listener) {
        super((FirebaseDatabase.getInstance().getReference().child("availableChats")));
        this.chatId = chatId;
        this.listener = listener;
    }

    /**
     * Retrieves a single chat room by ID.
     */
    @Override
    public void call() {
        DatabaseReference chatRoomRef = getBaseRef().child(chatId);
        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                listener.onSuccess(chatRoom);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(new FirebaseError(databaseError));
            }
        });
    }

}
