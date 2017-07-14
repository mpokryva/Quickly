package com.android.miki.quickly.utilities;

import android.support.annotation.NonNull;

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
        test -> ()
        Query query = (startingPoint == null) ? mAvailableChatsRef : mAvailableChatsRef.startAt(startingPoint.getId());
        // 1 is added to BATCH_SIZE so we can save the last as the marker for getRoom time.
        query.limitToFirst(BATCH_SIZE + 1).addListenerForSingleValueEvent(new ValueEventListener() {
            List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = chat.getValue(ChatRoom.class);
                    chatRooms.add(chatRoom);
                }
                if (chatRooms.size() > 0) {
                    startingPoint = chatRooms.remove(chatRooms.size() - 1); // Remove last room and set as starting point for getRoom batch.
                }
                listener.onSuccess(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(new FirebaseError(databaseError));
            }
        });
    }

    private void makeFirebaseCall(String param1, final FirebaseListener listener) {

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


    /**
     * Retrieves a single chat room by ID.
     *
     * @param chatId   The ID of the chat room to retrieve.
     * @param listener The callback listener to send data back to the client.
     */
    public void getChatRoom(String chatId, @NonNull final FirebaseListener<ChatRoom> listener) {
        BiConsumer<String, FirebaseListener<ChatRoom>> function = (first, second) ->
        DatabaseReference chatRoomRef = mAvailableChatsRef.child(chatId);
        listener.onLoading();
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
        makeFirebaseCall(function, listener);
    }

    public int getBatchSize() {
        return BATCH_SIZE;
    }

    private boolean isConnected(final FirebaseListener<Boolean> listener) {
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

}
