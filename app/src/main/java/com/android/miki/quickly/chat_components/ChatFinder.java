package com.android.miki.quickly.chat_components;

import android.support.annotation.NonNull;

import com.android.miki.quickly.utilities.DataGenerator;
import com.android.miki.quickly.utilities.Callback;
import com.android.miki.quickly.models.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DataGenerator dataGenerator = new DataGenerator();
    private final int BATCH_SIZE = 10;


    public void findChatRoomCallback(@NonNull final Callback<List<ChatRoom>> finishedCallback) {
        //dataGenerator.deleteAllData();
        //dataGenerator.createTestChats(30);
        mAvailableChatsRef.limitToFirst(BATCH_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
            List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = chat.getValue(ChatRoom.class);
                    chatRooms.add(chatRoom);
                }
                finishedCallback.callback(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
