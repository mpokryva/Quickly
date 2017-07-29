package com.android.miki.quickly.core.chat_room;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utilities.FirebaseError;
import com.android.miki.quickly.utilities.FirebaseListener;

import java.util.List;

/**
 * Created by mpokr on 7/4/2017.
 */

public class ChatRoomManager {

    private ChatRoomStorage storage;
    private ChatRoomFinder finder;
    private int itemsInCache;
    private int currentPosition;
    private int retrievalPosition;
    private static ChatRoomManager manager;


    private ChatRoomManager() {
        storage = new ChatRoomStorage();
        finder = new ChatRoomFinder();
        itemsInCache = finder.getBatchSize(); // For now, we are only caching objects retrieved in a single batch.
        currentPosition = 0;
        retrievalPosition = 0;
    }

    public void getRoom(int position, final FirebaseListener<ChatRoom> callBack) {
        callBack.onLoading();
        retrievalPosition = position % finder.getBatchSize();
        ChatRoom chatRoom = storage.getRoom(retrievalPosition);
        if (chatRoom != null) {
            callBack.onSuccess(chatRoom); // Chat room is in current batch.
        } else {
            // Need to pull chat room from Firebase. Either ID is cached, or
            // new batch needs to be retrieved altogether.
            String id = storage.getRoomId(position); // Check if ID is cached.
            if (id == null) { // ID not cached. Need to retrieve new batch.
                finder.getChatRoomBatch(batchListener(callBack));
            } else { // ID is cached.
                finder.getChatRoom(id, singleRoomListener(callBack));
            }
        }
    }

    private FirebaseListener<List<ChatRoom>> batchListener(final FirebaseListener<ChatRoom> listener) {
        return new FirebaseListener<List<ChatRoom>>() {
            @Override
            public void onLoading() {
                listener.onLoading();
            }

            @Override
            public void onSuccess(List<ChatRoom> rooms) {
                storage.cacheBatch(rooms);
                listener.onSuccess(storage.getRoom(retrievalPosition));
            }

            @Override
            public void onError(FirebaseError error) {
                listener.onError(error);
            }
        };
    }

    private FirebaseListener<ChatRoom> singleRoomListener(final FirebaseListener<ChatRoom> listener) {
        return new FirebaseListener<ChatRoom>() {
            @Override
            public void onLoading() {
                listener.onLoading();
            }

            @Override
            public void onSuccess(ChatRoom room) {
                listener.onSuccess(room);
            }

            @Override
            public void onError(FirebaseError error) {
                listener.onError(error);
            }
        };
    }

    public int getItemsInCache() {
        return itemsInCache;
    }

    public static ChatRoomManager getInstance() {
        if (manager == null) {
            manager = new ChatRoomManager();
        }
        return manager;
    }

}
