package com.android.miki.quickly;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoom implements Serializable{

    private String chatId;
    private long creationTimestamp;
    private int numParticipants;
    private Message lastMessage;

    public ChatRoom() {

    }

    public ChatRoom(String chatId, long creationTimeStamp, int participants, Message lastMessage) {
        this.creationTimestamp = creationTimeStamp;
        this.numParticipants = participants;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public String getChatId() {
        return chatId;
    }


}
