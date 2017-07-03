package com.android.miki.quickly.chat_components;

import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by mpokr on 7/2/2017.
 */

public interface ChatRoomObserver {

    void numUsersChanged(int numUsers);
    void userAdded(User user);
    void userRemoved(User user);
    void nameChanged(String name);
    void messageAdded(Message message);
}
