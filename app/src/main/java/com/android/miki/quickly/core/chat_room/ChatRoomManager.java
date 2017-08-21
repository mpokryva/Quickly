package com.android.miki.quickly.core.chat_room;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;

import java.util.List;

/**
 * Created by mpokr on 7/4/2017.
 */

public class ChatRoomManager {

    private ChatRoomFinder finder;
    private int itemsInCache;
    private int currentPosition;
    private int retrievalPosition;
    private static ChatRoomManager manager;


    private ChatRoomManager() {
        finder = new ChatRoomFinder();
        itemsInCache = finder.getBatchSize(); // For now, we are only caching objects retrieved in a single batch.
        currentPosition = 0;
        retrievalPosition = 0;
    }

    public void getRoom(int position, final FirebaseListener<ChatRoom> callBack) {
        finder.get
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
