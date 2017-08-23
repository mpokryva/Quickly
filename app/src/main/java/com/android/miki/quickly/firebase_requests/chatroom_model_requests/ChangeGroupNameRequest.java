package com.android.miki.quickly.firebase_requests.chatroom_model_requests;

import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by mpokr on 8/16/2017.
 */

public class ChangeGroupNameRequest extends SingleChatRoomRequest {

    /**
     * The chat room ID
     */
    private String roomId;
    private String newName;

    public ChangeGroupNameRequest(String roomId, String newName) {
        this.roomId = roomId;
        this.newName = newName;
    }

    @Override
    public DatabaseReference ref() {
        return DatabaseReferences.AVAILABLE_CHATS.child(roomId).child("name");
    }

    @Override
    public Object value() {
        return newName;
    }
}
