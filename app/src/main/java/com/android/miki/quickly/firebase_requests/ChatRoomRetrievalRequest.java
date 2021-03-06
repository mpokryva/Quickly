package com.android.miki.quickly.firebase_requests;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.models.ChatRoom;
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

public class ChatRoomRetrievalRequest implements FirebaseRequest, Callable<ChatRoom> {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    /**
     * The ID of the chat room to retrieve.
     */
    private String chatId;

    public ChatRoomRetrievalRequest(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Retrieves a single chat room by ID.
     */
    @Override
    public void call(final FirebaseListener<ChatRoom> listener) {
        ref().addListenerForSingleValueEvent(new ValueEventListener() {
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

    @Override
    public DatabaseReference ref() {
        return DatabaseReferences.AVAILABLE_CHATS.child(chatId);
    }

}
