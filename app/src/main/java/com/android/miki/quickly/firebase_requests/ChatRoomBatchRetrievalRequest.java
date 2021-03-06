package com.android.miki.quickly.firebase_requests;

import com.android.miki.quickly.core.Callable;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Miki on 7/14/2017.
 */

public class ChatRoomBatchRetrievalRequest implements FirebaseRequest, Callable<List<ChatRoom>> {

    private ChatRoom startingPointRoom;
    private int batchSize;

    public ChatRoomBatchRetrievalRequest(int batchSize) {
        this.startingPointRoom = null;
        this.batchSize = batchSize;
    }

    @Override
    public void call(final FirebaseListener<List<ChatRoom>> listener) {
        Query query = (startingPointRoom == null) ? ref() : ref().startAt(startingPointRoom.getId());
        // 1 is added to BATCH_SIZE so we can save the last as the marker for getRoom time.
        query.limitToFirst(batchSize + 1).addListenerForSingleValueEvent(new ValueEventListener() {
            List<ChatRoom> chatRooms = new ArrayList<ChatRoom>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = chat.getValue(ChatRoom.class);
                    chatRooms.add(chatRoom);
                }
                if (chatRooms.size() > 0) {
                    startingPointRoom = chatRooms.remove(chatRooms.size() - 1); // TODO: Move this call to ChatFinder
                    // Remove last room and set as starting point for getRoom batch.
                }
                listener.onSuccess(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(new FirebaseError(databaseError));
            }
        });
    }

    @Override
    public DatabaseReference ref() {
        return DatabaseReferences.AVAILABLE_CHATS;
    }

}
