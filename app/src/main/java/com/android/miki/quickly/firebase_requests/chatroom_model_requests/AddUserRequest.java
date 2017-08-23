package com.android.miki.quickly.firebase_requests.chatroom_model_requests;

import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.google.firebase.database.DatabaseReference;

import java.util.LinkedHashMap;

/**
 * Created by mpokr on 8/20/2017.
 */

public class AddUserRequest extends ChatRoomRequestGroup<Void> {

    private User user;

    public AddUserRequest(ChatRoom chatRoom, User user) {
        super(chatRoom);
        this.user = user;
    }

    @Override
    LinkedHashMap<DatabaseReference, Object> requests() {
        LinkedHashMap<DatabaseReference, Object> requests = new LinkedHashMap<>();
        DatabaseReference usersRef = DatabaseReferences.AVAILABLE_CHATS.child(getChatRoom().getId()).child(FirebaseRefKeys.USERS).child(user.getId());
        requests.put(usersRef, user);
        DatabaseReference numUsersRef = DatabaseReferences.AVAILABLE_CHATS.child(getChatRoom().getId()).child(FirebaseRefKeys.NUM_USERS);
        requests.put(numUsersRef, getChatRoom().getNumUsers() + 1);
        return requests;
    }
}
