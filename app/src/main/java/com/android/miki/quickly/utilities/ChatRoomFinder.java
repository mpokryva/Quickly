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
import java.util.List;

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
     * Retrieves the nextRoom batch of chat rooms.
     *
     * @param voidCallback Callback the client should use to do something with the chat rooms returned.
     */
    public void getChatRoomBatch(@NonNull final VoidCallback<List<ChatRoom>> voidCallback) {
        //dataGenerator.deleteAllData();
        //dataGenerator.createTestChats(30);
        Query query = (startingPoint == null) ? mAvailableChatsRef : mAvailableChatsRef.startAt(startingPoint.getId());
        // 1 is added to BATCH_SIZE so we can save the last as the marker for nextRoom time.
        query.limitToFirst(BATCH_SIZE + 1).addListenerForSingleValueEvent(new ValueEventListener() {
            List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = chat.getValue(ChatRoom.class);
                    chatRooms.add(chatRoom);
                }
                if (chatRooms.size() > 0) {
                    startingPoint = chatRooms.remove(chatRooms.size() - 1); // Remove last room and set as starting point for nextRoom batch.
                }
                voidCallback.done(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getChatRoom(String chatId, @NonNull final VoidCallback<ChatRoom> voidCallback) {
        DatabaseReference chatRoomRef = mAvailableChatsRef.child(chatId);
        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                voidCallback.done(chatRoom);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getBatchSize() {
        return BATCH_SIZE;
    }

}
