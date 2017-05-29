package com.android.miki.quickly.models;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoom implements Serializable {

    private String chatId;
    private long creationTimestamp;
    private int numUsers;
    private HashMap<String, User> users = new HashMap<>();
    private Message lastMessage;
    private transient FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private transient DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private transient DatabaseReference mMessagesRef = mDatabase.getReference().child("messages");

    public ChatRoom() {

    }

    public ChatRoom(String chatId, long creationTimeStamp, HashMap<String, User> users, Message lastMessage) {
        this.creationTimestamp = creationTimeStamp;
        this.users = users;
        this.numUsers = (users == null) ? 0 : users.size();
        this.lastMessage = lastMessage;
        this.chatId = chatId;
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

    public String getChatId() {
        return chatId;
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
            numUsers++;
            mAvailableChatsRef.child(this.chatId).child("users").child(user.getUserId()).setValue(user);
            mAvailableChatsRef.child(this.chatId).child("numUsers").setValue(numUsers);
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
            }
            numUsers--;
            mAvailableChatsRef.child(this.chatId).child("users").child(user.getUserId()).removeValue();
            mAvailableChatsRef.child(this.chatId).child("numUsers").setValue(numUsers);
        }

    }

    public void addMessage(Message message) {
        if (message != null) {
            mMessagesRef.child(this.getChatId()).push().setValue(message);
            mAvailableChatsRef.child(this.getChatId()).child("lastMessage").setValue(message);
        }
    }


}
