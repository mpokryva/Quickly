package com.android.miki.quickly.utilities;

import com.android.miki.quickly.models.ChatRoom;

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

    public void getRoom(final VoidCallback<ChatRoom> callBack) {
        final int storagePosition = retrievalPosition % finder.getBatchSize();
        ChatRoom chatRoom = storage.getRoom(storagePosition);
        if (chatRoom != null) {
            callBack.done(chatRoom); // Chat room is in current batch.
        } else {
            // Need to pull chat room from Firebase. Either ID is cached, or
            // new batch needs to be retrieved altogether.
            String id = storage.getRoomId(retrievalPosition); // Check if ID is cached.
            if (id == null) { // ID not cached. Need to retrieve new batch.
                finder.getChatRoomBatch(new VoidCallback<List<ChatRoom>>() {
                    @Override
                    public void done(List<ChatRoom> rooms) {
                        storage.cacheBatch(rooms);
                        callBack.done(storage.getRoom(storagePosition));
                    }
                });
            } else { // ID is cached.
                finder.getChatRoom(id, new VoidCallback<ChatRoom>() {
                    @Override
                    public void done(ChatRoom room) {
                        callBack.done(room);
                    }
                });
            }
        }
    }

    public void setPosition(int position) {
        retrievalPosition = position;
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
