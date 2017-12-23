package com.android.miki.quickly.core.chat_room;

import android.support.annotation.NonNull;

import com.android.miki.quickly.core.network.FirebaseClient;
import com.android.miki.quickly.firebase_requests.ChatRoomBatchRetrievalRequest;
import com.android.miki.quickly.firebase_requests.ChatRoomRetrievalRequest;
import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.DataGenerator;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoomFinder {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DataGenerator dataGenerator;
    private final boolean IS_TESTING = true;
    private final int BATCH_SIZE = 100;
    private HashMap<Integer, String> positionToIdMap;
    private ChatRoom startingPointRoom;
    private static ChatRoomFinder roomFinder;


    private ChatRoomFinder() {
        positionToIdMap = new HashMap<>();
        dataGenerator = new DataGenerator();
        if (IS_TESTING) {
            dataGenerator.deleteAllData();
            dataGenerator.createTestChats(30);
            int i = 0;
        }
        DatabaseReferences.AVAILABLE_CHATS_COUNTER.keepSynced(true);
    }

    public static ChatRoomFinder getInstance() {
        if (roomFinder == null) {
            roomFinder = new ChatRoomFinder();
        }
        return roomFinder;
    }

    /**
     * Retrieves the getRoom batch of chat rooms.
     *
     * @param listener Callback the client should use to do something with the chat rooms returned, or to handle
     *                 the error.
     */
    public void getChatRoomBatch(@NonNull final FirebaseListener<List<ChatRoom>> listener) {
        FirebaseClient client = FirebaseClient.getInstance();
//        client.execute(new ChatRoomBatchRetrievalRequest(BATCH_SIZE), listener);
        new ChatRoomBatchRetrievalRequest(BATCH_SIZE).call(listener);
    }


    /**
     * Retrieves a single chat room by ID.
     *
     * @param chatId   The ID of the chat room to retrieve.
     * @param listener The callback listener to send data back to the client.
     */
    public void getChatRoom(String chatId, @NonNull final FirebaseListener<ChatRoom> listener) {
        FirebaseClient client = FirebaseClient.getInstance();
        client.execute(new ChatRoomRetrievalRequest(chatId), listener);
    }

    public void getRoom(final int position, final FirebaseListener<ChatRoom> listener) {
        final DatabaseReference availableChatsRef = DatabaseReferences.AVAILABLE_CHATS;
        String roomId = positionToIdMap.get(position);
        if (roomId != null) {
            availableChatsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    positionToIdMap.put(position, chatRoom.getId());
                    listener.onSuccess(chatRoom);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listener.onError(new FirebaseError(databaseError));
                }
            });
        } else {
            Query query = availableChatsRef;//(startingPointRoom == null) ? availableChatsRef : availableChatsRef.startAt(startingPointRoom.getId());
            // 1 is added to BATCH_SIZE so we can save the last as the marker for getRoom next time.
            query.orderByChild(FirebaseRefKeys.NUM_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<ChatRoom> chatRooms = new ArrayList<>();
                    for (DataSnapshot chat : dataSnapshot.getChildren()) {
                        ChatRoom chatRoom = chat.getValue(ChatRoom.class);
                        if (chatRoom != null) {
                            chatRooms.add(chatRoom);
                        }
                    }
                    Random r = new Random();
                    int randomPosition = r.nextInt(chatRooms.size());
                    ChatRoom room = chatRooms.get(randomPosition);
                    positionToIdMap.put(position, room.getId());
                    startingPointRoom = room;
                    listener.onSuccess(room);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listener.onError(new FirebaseError(databaseError));
                }
            });
        }
    }

    public int getNumberOfAccessedRooms() {
        return positionToIdMap.size();
    }


    public int getBatchSize() {
        return BATCH_SIZE;
    }

}
