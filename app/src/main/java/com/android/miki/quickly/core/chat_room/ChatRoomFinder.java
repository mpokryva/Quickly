package com.android.miki.quickly.core.chat_room;

import android.support.annotation.NonNull;

import com.android.miki.quickly.core.network.FirebaseClient;
import com.android.miki.quickly.firebase_queries.ChatRoomBatchQuery;
import com.android.miki.quickly.firebase_queries.ChatRoomQuery;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.DataGenerator;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoomFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DataGenerator dataGenerator;
    private final boolean IS_TESTING = true;
    private final int BATCH_SIZE = 10;
    private HashMap<Integer, String> positionToIdMap;

    public ChatRoomFinder() {
        positionToIdMap = new HashMap<>();
        dataGenerator = new DataGenerator();
        if (IS_TESTING) {
            dataGenerator.deleteAllData();
            dataGenerator.createTestChats(30);
            int i =0;
        }
    }

    /**
     * Retrieves the getRoom batch of chat rooms.
     *
     * @param listener Callback the client should use to do something with the chat rooms returned, or to handle
     *                 the error.
     */
    public void getChatRoomBatch(@NonNull final FirebaseListener<List<ChatRoom>> listener) {
        FirebaseClient client = FirebaseClient.getInstance();
        client.queryFirebase(new ChatRoomBatchQuery(BATCH_SIZE), listener);
    }

    /**
     * Retrieves a single chat room by ID.
     *
     * @param chatId   The ID of the chat room to retrieve.
     * @param listener The callback listener to send data back to the client.
     */
    public void getChatRoom(String chatId, @NonNull final FirebaseListener<ChatRoom> listener) {
        FirebaseClient client = FirebaseClient.getInstance();
        client.queryFirebase(new ChatRoomQuery(chatId), listener);
    }

    public void getRoom(int position) {
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

    public int getBatchSize() {
        return BATCH_SIZE;
    }

}
