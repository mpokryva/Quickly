package com.android.miki.quickly.utilities;

import com.android.miki.quickly.models.ChatRoom;

import java.util.List;

/**
 * Created by mpokr on 7/4/2017.
 */

public class ChatRoomManager {

    private ChatRoomStorage storage;
    private ChatRoomFinder finder;
    private int currentPosition;

    public ChatRoomManager() {
        storage = new ChatRoomStorage();
        finder = new ChatRoomFinder();
        currentPosition = 0;
    }

    public void nextRoom(int position, final VoidCallback<ChatRoom> voidCallback) {
        final int storagePosition = position % finder.getBatchSize();
        ChatRoom chatRoom = storage.getRoom(storagePosition);
        if (chatRoom != null) {
            voidCallback.done(chatRoom); // Chat room is in current batch.
        } else {
            // Need to pull chat room from Firebase. Either ID is cached, or
            // new batch needs to be retrieved altogether.
            String id = storage.getRoomId(position); // Check if ID is cached.
            if (id == null) { // ID not cached. Need to retrieve new batch.
                finder.getChatRoomBatch(new VoidCallback<List<ChatRoom>>() {
                    @Override
                    public void done(List<ChatRoom> rooms) {
                        storage.cacheBatch(rooms);
                        voidCallback.done(storage.getRoom(storagePosition));
                    }
                });
            } else { // ID is cached.
                finder.getChatRoom(id, new VoidCallback<ChatRoom>() {
                    @Override
                    public void done(ChatRoom room) {
                        voidCallback.done(room);
                    }
                });
            }
        }
    }
}
