package com.android.miki.quickly;

import android.support.annotation.NonNull;
import android.support.v7.widget.SnapHelper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");




    public void findChatRoomCallback(@NonNull final SimpleCallback<List<ChatRoom>> finishedCallback) {
        //createTestChats(30);
        mAvailableChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                    chatRooms.add(chat.getValue(ChatRoom.class));
                }
                finishedCallback.callback(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void pushTestChat(int numParticipants) {
        DatabaseReference newChatRef = mAvailableChatsRef.push();
        String chatId = newChatRef.getKey();
        ChatRoom room = new ChatRoom(chatId, System.currentTimeMillis(), numParticipants, new Message(System.currentTimeMillis(), "Bob", "Yooooo"));
        newChatRef.setValue(room);
    }

    private void createTestChats(int numChats) {
        for (int i = 0; i < numChats; i++) {
            pushTestChat(i);
        }
    }


}
