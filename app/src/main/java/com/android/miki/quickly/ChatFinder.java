package com.android.miki.quickly;

import android.support.annotation.NonNull;
import android.support.v7.widget.SnapHelper;
import android.view.DragAndDropPermissions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DataGenerator dataGenerator = new DataGenerator();



    public void findChatRoomCallback(@NonNull final SimpleCallback<List<ChatRoom>> finishedCallback) {
        //dataGenerator.createTestChats(30);
        mAvailableChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
