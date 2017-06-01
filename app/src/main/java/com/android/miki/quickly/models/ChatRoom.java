package com.android.miki.quickly.models;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoom implements Serializable {

    private String id;
    private long creationTimestamp;
    private int numUsers;
    private HashMap<String, User> users = new HashMap<>();
    private Message lastMessage;
    private transient FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private transient DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private transient DatabaseReference mMessagesRef = mDatabase.getReference().child("messages");
    private static final String TAG = "ChatRoom";

    public ChatRoom() {

    }

    public ChatRoom(String id, long creationTimeStamp, HashMap<String, User> users, Message lastMessage) {
        this.creationTimestamp = creationTimeStamp;
        this.users = users;
        this.numUsers = (users == null) ? 0 : users.size();
        this.lastMessage = lastMessage;
        this.id = id;
        ;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    /**
     * Adds user to this ChatRoom's list of users, increments numUsers, and pushes the data to Firebase
     *
     * @param user The user to add
     */
    public void addUser(User user) {
        if (user != null) {
            users.put(user.getUserId(), user);
            numUsers = users.size();
            mAvailableChatsRef.child(id).child("users").child(user.getUserId()).setValue(user);
            mAvailableChatsRef.child(id).child("numUsers").setValue(numUsers);
        }
    }

    /**
     * Removes user from this ChatRoom's list of users, decrements numUsers, and pushes the data to Firebase
     *
     * @param user The user to remove
     */
    public void removeUser(User user) {
        if (user != null) {
            User removedUser = users.remove(user.getUserId());
            if (removedUser == null) {
                Log.e("removeUser", "User to remove was not found in user map.");
            } else {
                numUsers = users.size();
                mAvailableChatsRef.child(id).child("users").child(user.getUserId()).removeValue();
                mAvailableChatsRef.child(id).child("numUsers").setValue(numUsers);
            }
        }

    }

    public void addMessage(Message message) {
        if (message != null) {
            DatabaseReference messageRef = mMessagesRef.child(id).push();
            message.setMessageIdOnce(messageRef.getKey());
            messageRef.setValue(message);
            mAvailableChatsRef.child(this.id).child("lastMessage").setValue(message);
        }
    }

    public void removeMessage(Message message) {
        if (message != null) {
            String messageId = message.getId();
            if (messageId != null) {
                mMessagesRef.child(this.id).child(messageId).removeValue();
                Query lastQuery = mMessagesRef.orderByChild("timestamp").limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Message newLastMessage = dataSnapshot.getValue(Message.class);
                        mAvailableChatsRef.child(ChatRoom.this.id).child("lastMessage").setValue(newLastMessage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                Log.e(TAG, "Message ID not initialized");
            }
        }
    }


}
