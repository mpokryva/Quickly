package com.android.miki.quickly.core.chat_room;

import com.android.miki.quickly.models.ChatRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mpokr on 7/3/2017.
 */

public class ChatRoomStorage {

    /**
     * Cache of chat room IDs retrieved from Firebase.
     * Maps position in viewpager to chat room ID.
     */
    private List<String> positionToIdList;

    /**
     * Set of chat room IDs
     */
    private Set<String> idSet;

    /**
     * Cache of ChatRoom objects.
     */
    private List<ChatRoom> chatRooms;

    public ChatRoomStorage() {
        positionToIdList = new ArrayList<>();
        idSet = new HashSet<>();
        chatRooms = new ArrayList<>();
    }

    /**
     * Caches a batch of chat rooms. Caches IDs for later use, and caches the ChatRoom objects
     * for use now, until new batch needs to be retrieved.
     * @param chatRooms The batch of rooms to cache.
     */
    public void cacheBatch(List<ChatRoom> chatRooms) {
        for (int i = 0; i < chatRooms.size(); i++) {
            String id = chatRooms.get(i).getId();
            if (!idSet.contains(id)) {
                positionToIdList.add(id); // If set of IDs does not contain this ID, cache it.
                idSet.add(id);
            }
            this.chatRooms = chatRooms; // Overwrite the current chat room batch with the new one
        }
    }


    public String getRoomId(int position) {
        if (position < positionToIdList.size()) {
            return positionToIdList.get(position);
        } else {
            return null;
        }
    }

    public String getLastId() {
        return positionToIdList.get(positionToIdList.size() - 1);
    }

    public ChatRoom getRoom(int position) {
        if (position < chatRooms.size()) {
            return chatRooms.get(position);
        } else {
            return null;
        }
    }


}
