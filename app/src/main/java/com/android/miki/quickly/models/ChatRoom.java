package com.android.miki.quickly.models;

import android.graphics.Color;
import android.util.Log;

import com.android.miki.quickly.chat_components.ChatRoomObserver;
import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.utils.ColorGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRoom implements Serializable {

    private transient FirebaseDatabase database = FirebaseDatabase.getInstance();
    private transient DatabaseReference availableChatsRef = DatabaseReferences.AVAILABLE_CHATS;
    private transient DatabaseReference chatRef;
    private transient DatabaseReference messagesRef = database.getReference().child("messages");
    private transient static final String TAG = "ChatRoom";
    private String id;
    private Long creationTimestamp;
    private int numUsers;
    private HashMap<String, User> users = new HashMap<>();
    private Message lastMessage;
    private String name;
    private transient List<ChatRoomObserver> observers = new ArrayList<>();
    private HashMap<String, Integer> userIdColorMap = new HashMap<>();


    public ChatRoom() {

    }

    public ChatRoom(String id, Message lastMessage) {
        this.users = new HashMap<>();
        this.numUsers = (users == null) ? 0 : users.size();
        this.lastMessage = lastMessage;
        this.id = id;
        observers = new ArrayList<>();
        chatRef = availableChatsRef.child(id);
    }

    public Map<String, String> getCreationTimestamp() {
        return ServerValue.TIMESTAMP;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public Map<String, Integer> getUserIdColorMap() {
        return userIdColorMap;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public Iterator<User> userIterator() {
        return users.values().iterator();
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the default name of the room (comma separated list of all users in the chat room).
     *
     * @return The default name of the room.
     */
    @Exclude
    public String getDefaultName() {
        Iterator<User> it = userIterator();
        String defaultName = "";
        while (it.hasNext()) {
            User user = it.next();
            defaultName += user.getDisplayName() + ", ";
        }
        int lastIndex = defaultName.lastIndexOf(", ");
        if (lastIndex >= 0) {
            defaultName = defaultName.substring(0, lastIndex);
        }
        return defaultName;
    }

    /*
    Below are methods that call the Firebase API.
     */

    public void changeName(final String name) {
        ChatRoom.this.name = name;
        chatRef.child("name").setValue(name, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    for (ChatRoomObserver observer : observers) {
                        observer.nameChanged(name);
                    }
                }
            }
        });
    }

    /**
     * Adds user to this ChatRoom's list of users, increments numUsers, and pushes the data to Firebase
     *
     * @param user The user to add
     */
    public void addUser(final User user) {
        if (user != null) {
            DatabaseReference usersRef = DatabaseReferences.AVAILABLE_CHATS.child(id)
                    .child(FirebaseRefKeys.USERS).child(user.getId());
            DatabaseReference numUsersRef = DatabaseReferences.AVAILABLE_CHATS.child(id).
                    child(FirebaseRefKeys.NUM_USERS);
            users.put(user.getId(), user);
            addUserColor(user);
            usersRef.setValue(user, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        for (ChatRoomObserver observer : observers) {
                            observer.userAdded(user);
                        } // TODO: Maybe move to second call?
                    }
                }
            });
            numUsers = users.size();
            numUsersRef.setValue(users.size());
        }
    }

    public void addUserColor(User user) {
        ColorGenerator colorGenerator = new ColorGenerator();
        int[] randomColors = colorGenerator.goldenRationPalette(Color.rgb(255, 255, 255), users.size());
        userIdColorMap.put(user.getId(), randomColors[randomColors.length - 1]);
        DatabaseReference userColorsRef = DatabaseReferences.AVAILABLE_CHATS.child(id)
                .child(FirebaseRefKeys.USER_ID_COLOR_MAP);
        userColorsRef.setValue(userIdColorMap);
    }


    public void removeUserColor(User user) {
        boolean removed = (userIdColorMap.remove(user.getId()) != null);
        if (removed) {
            DatabaseReference userColorsRef = DatabaseReferences.AVAILABLE_CHATS.child(id)
                    .child(FirebaseRefKeys.USER_ID_COLOR_MAP);
            userColorsRef.setValue(userIdColorMap);
        }
    }

    /**
     * Removes user from this ChatRoom's list of users, decrements numUsers, and pushes the data to Firebase
     *
     * @param user The user to remove
     */
    public void removeUser(final User user) {
        if (user != null) {
            users.remove(user.getId());
            availableChatsRef.child(id).child("users").child(user.getId()).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        for (ChatRoomObserver observer : observers) {
                            observer.userRemoved(user);
                        }
                    }
                }
            });
            removeUserColor(user);
            availableChatsRef.child(id).child("numUsers").setValue(users.size(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    numUsers = users.size();
                }
            });



        }

    }

    public void addMessage(final Message message) {
        if (message != null) {
            DatabaseReference messageRef = messagesRef.child(id).push();
            message.setMessageIdOnce(messageRef.getKey());
            messageRef.setValue(message, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        for (ChatRoomObserver observer : observers) {
                            observer.messageAdded(message);
                        }
                    }
                    availableChatsRef.child(ChatRoom.this.id).child("lastMessage").setValue(message);
                }
            });
        }
    }

    public void removeMessage(Message message) {
        if (message != null) {
            String messageId = message.getId();
            if (messageId != null) {
                messagesRef.child(this.id).child(messageId).removeValue();
                Query lastQuery = messagesRef.orderByChild("timestamp").limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Message newLastMessage = dataSnapshot.getValue(Message.class);
                        availableChatsRef.child(ChatRoom.this.id).child("lastMessage").setValue(newLastMessage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                Log.e(TAG, "Message ID not initialized");
            }
        }
    }

    /**
     * Adds an observer to observe this chat room. If the observer
     * is a fragment/activity/similar Android class, it must remove itself from the observer list
     * in its onDestroy() method.
     *
     * @param observer
     */
    public void addObserver(ChatRoomObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(observer);
    }

    public void removeObserver(ChatRoomObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.remove(observer);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        observers = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        availableChatsRef = DatabaseReferences.AVAILABLE_CHATS;
        messagesRef = database.getReference().child("messages");
    }


}
