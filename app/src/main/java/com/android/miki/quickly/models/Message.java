package com.android.miki.quickly.models;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by mpokr on 5/22/2017.
 */

public class Message implements Serializable{

    private Long timestamp;
    private User sender;
    private String messageText;
    private Gif gif;
    private String id;


    public Message(User sender, String messageText) {
        this.sender = sender;
        this.messageText = messageText;
        this.gif = null;
    }

    public Message(User sender, Gif gif) {
        this.gif = gif;
        this.sender = sender;
        this.messageText = null;
    }

    public Message() {

    }

    public Map<String, String> getTimestamp() {
        return ServerValue.TIMESTAMP;
    }

    public User getSender() {
        return sender;
    }

    public String getMessageText() {
        return messageText;
    }

    public Gif getGif() {
        return gif;
    }

    protected void setMessageIdOnce(String messageId) {
        if (this.id == null) {
            this.id = messageId;
        }
    }

    public String getId() {
        return id;
    }

//    public String timeString() {
//        Calendar calendar = GregorianCalendar.getInstance();
//        calendar.setTimeInMillis(this.getTimestamp());
//        int hours = calendar.get(Calendar.HOUR_OF_DAY);
//        String amOrPm = (hours < 12) ? " AM" : " PM";
//        if (hours > 12) {
//            hours -= 12;
//        }
//        int minutes = calendar.get(Calendar.MINUTE);
//        return hours + ":" + minutes + amOrPm;
//    }
}
