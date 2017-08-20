package com.android.miki.quickly.utils;

import android.net.Uri;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by mpokr on 5/26/2017.
 */

public class DataGenerator {


    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mAvailableChatsRef = mDatabase.getReference().child("availableChats");
    private DatabaseReference mMessagesRef = mDatabase.getReference().child("messages");
    private DatabaseReference mUsersRef = mDatabase.getReference().child("users");
    // ** Field "randomSentences" is at the bottom **

    private void pushTestChat() {
        DatabaseReference newChatRef = mAvailableChatsRef.push();
        String chatId = newChatRef.getKey();
        List<String> userNames = Name.randomNameList();
        Random r = new Random();
        HashMap<String, User> users = new HashMap<>();
        User randomUser = null;
        for (String userName : userNames) {
            String university = "Stony Brook University";
            String url = "https://www.google.com";
            User user = new User(userName, url);
            users.put(user.getUserId(), user); // Add user to user list
            randomUser = user;
        }
        String lastMessageText = randomSentences[r.nextInt(randomSentences.length)]; // Get random message
        Message lastMessage;
        if (randomUser == null) {
            lastMessage = null;
        } else {
            lastMessage = new Message(System.currentTimeMillis(), randomUser, lastMessageText);;
        }
        ChatRoom chatRoom = new ChatRoom(chatId, System.currentTimeMillis(), users , lastMessage);
        Iterator<User> it = users.values().iterator();
        while (it.hasNext()) {
            User user = it.next();
            String text = randomSentences[r.nextInt(randomSentences.length)];
            Message message = new Message(System.currentTimeMillis(), user, text);
            chatRoom.addMessage(message);
        }
        chatRoom.addMessage(lastMessage);
        //chatRoom.changeName("Test name");
        newChatRef.setValue(chatRoom); // Push chat room info

    }

    public void createTestChats(int numChats) {
        for (int i = 0; i < numChats; i++) {
            pushTestChat();
        }
    }

    public void deleteAllData() {
        mAvailableChatsRef.removeValue();
        mMessagesRef.removeValue();
        mUsersRef.removeValue();
    }

    private enum Name {
        ALEX("Alex"),
        BOB("Bob"),
        MIKI("Miki"),
        DAN("Dan"),
        DAVID("David"),
        SARAH("Sarah"),
        KATE("Kate"),
        JOE("Joe");

        private String name;

        Name(String name) {
            this.name = name;
        }

        public static String randomName() {
            Name[] names = Name.class.getEnumConstants();
            Random r = new Random();
            int index = r.nextInt(names.length);
            return names[index].name;
        }

        public static List<String> randomNameList() {
            Name[] names = Name.class.getEnumConstants();
            Random r = new Random();
            int listSize = r.nextInt(names.length);
            List<String> nameList = new ArrayList<>();
            for (int i = 0; i < listSize; i++) {
                nameList.add(names[r.nextInt(names.length)].name);
            }
            return nameList;
        }
    }













    private final String[] randomSentences = new String[]{"I love eating toasted cheese and tuna sandwiches.", "If I don’t like something, I’ll stay away from it.",
            "They got there early, and they got really good seats.",
            "Cats are good pets, for they are clean and are not noisy.",
            "He ran out of money, so he had to stop playing poker.",
            "The sky is clear; the stars are twinkling.",
            "Last Friday in three week’s time I saw a spotted striped blue worm shake hands with a legless lizard.",
            "Lets all be unique together until we realise we are all the same.",
            "I checked to make sure that he was still alive.",
            "We need to rent a room for our party.",
            "Joe made the sugar cookies; Susan decorated them.",
            "The clock within this blog and the clock on my laptop are 1 hour different from each other.",
            "I love eating toasted cheese and tuna sandwiches.",
            "Should we start class now, or should we wait for everyone to get here?",
            "I hear that Nancy is very pretty.\n",
            "Sometimes, all you need to do is completely make an ass of yourself and laugh it off to realise that life isn’t so bad after all.",
            "We have a lot of rain in June.",
            "The stranger officiates the meal.",
            "There was no ice cream in the freezer, nor did they have money to go to the store.",
            "Don't step on the broken glass.",
            "Yeah, I think it's a good environment for learning English."
    };

}
