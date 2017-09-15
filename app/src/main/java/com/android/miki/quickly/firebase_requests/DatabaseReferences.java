package com.android.miki.quickly.firebase_requests;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mpokr on 8/20/2017.
 */

public class DatabaseReferences {

    private static final FirebaseDatabase root = FirebaseDatabase.getInstance();
    public static final DatabaseReference AVAILABLE_CHATS = root.getReference().child("availableChats");
    public static final DatabaseReference AVAILABLE_CHATS_COUNTER = root.getReference().child("availableChatsCounter");
    public static final DatabaseReference USERS = root.getReference().child(FirebaseRefKeys.USERS);

}
