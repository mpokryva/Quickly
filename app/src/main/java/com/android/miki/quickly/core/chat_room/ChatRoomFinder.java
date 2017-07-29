package com.android.miki.quickly.core.chat_room;

import android.support.annotation.NonNull;

import com.android.miki.quickly.core.network.FirebaseClient;
import com.android.miki.quickly.firebase_queries.ChatRoomBatchQuery;
import com.android.miki.quickly.firebase_queries.ChatRoomQuery;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utilities.DataGenerator;
import com.android.miki.quickly.utilities.FirebaseListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoomFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DataGenerator dataGenerator;
    private final int BATCH_SIZE = 10;

    public ChatRoomFinder() {
        dataGenerator = new DataGenerator();
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

    public int getBatchSize() {
        return BATCH_SIZE;
    }

}
