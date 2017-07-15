package com.android.miki.quickly.utilities;

import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.android.miki.quickly.core.FirebaseClient;
import com.android.miki.quickly.firebase_queries.ChatRoomBatchQuery;
import com.android.miki.quickly.firebase_queries.ChatRoomQuery;
import com.android.miki.quickly.models.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoomFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DataGenerator dataGenerator;
    private final int BATCH_SIZE = 10;
    private ChatRoom startingPoint;

    public ChatRoomFinder() {
        startingPoint = null;
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
        client.queryFirebase(new ChatRoomBatchQuery(startingPoint, BATCH_SIZE), listener);
    }



    /**
     * Retrieves a single chat room by ID.
     *
     * @param chatId   The ID of the chat room to retrieve.
     * @param listener The callback listener to send data back to the client.
     */
    public void getChatRoom(String chatId, @NonNull final FirebaseListener<ChatRoom> listener) {
        FirebaseClient client = FirebaseClient.getInstance();
        client.queryFirebase(new ChatRoomQuery(chatId, listener), listener);
    }

    public int getBatchSize() {
        return BATCH_SIZE;
    }

}
