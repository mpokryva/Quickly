package com.android.miki.quickly;

import java.io.Serializable;

/**
 * Created by mpokr on 5/22/2017.
 */

public class Message implements Serializable{

    private long timestamp;
    private String sender;
    private String messageText;


    public Message(long timestamp, String sender, String messageText) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.messageText = messageText;
    }

    public Message() {

    }

    public Message(long timestamp) {
        this.timestamp = timestamp;
        this.sender = null;
        this.messageText = null;
    }

    public static Message newMessage() {
        return new Message(System.currentTimeMillis());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessageText() {
        return messageText;
    }
}
