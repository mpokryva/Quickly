package com.android.miki.quickly;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoom implements Serializable{

    private String chatId;
    private long creationTimestamp;
    private int numUsers;
    private List<User> users;
    private Message lastMessage;

    public ChatRoom() {

    }

    public ChatRoom(String chatId, long creationTimeStamp, int participants, Message lastMessage) {
        this.creationTimestamp = creationTimeStamp;
        this.numUsers = participants;
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

    public void addUser(User user) {
        users.add(user);
        numUsers++;
    }

}
