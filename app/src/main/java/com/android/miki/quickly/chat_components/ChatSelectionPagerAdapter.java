package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.android.miki.quickly.core.FirebaseFragment;
import com.android.miki.quickly.core.chat_room.ChatRoomFinder;
import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionPagerAdapter extends FragmentStatePagerAdapter {

    private ChatRoomFinder chatRoomFinder;
    private ChatRoom currentChatRoom;
    private FragmentManager fm;
    private static final String TAG = ChatSelectionPagerAdapter.class.getName();
    private int currentPosition;
    private Context mContext;
    private boolean isConnected;
    private ChatFragment currentFragment;
    private boolean shouldForceDisconnect;
    private CustomViewPager viewPager;
    private User user;


    public ChatSelectionPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.fm = fm;
        this.user = User.currentUser();
        chatRoomFinder = ChatRoomFinder.getInstance();
        mContext = context;
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new ChatFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((ChatFragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public void loadRoom(final CustomViewPager container, final int position, final FirebaseListener <ChatRoom> listener) {
        viewPager = container;
        final ChatFragment fragment = instantiateItem(container, position);
        if (position == container.getCurrentItem()) {
            currentFragment = fragment;
        }
        Log.d(TAG, "isConnected from notifier: " + isConnected);

        chatRoomFinder.getRoom(position, new FirebaseListener<ChatRoom>() {
            @Override
            public void onLoading() {
                //fragment.setState(FirebaseFragment.LOADING);
                listener.onLoading();
            }

            // Connected to internet (according to ConnectivityNotifer), but not able to complete request.
            @Override
            public void onError(FirebaseError error) {
                //fragment.setState(FirebaseFragment.ERROR);
                listener.onError(error);
                Log.d(TAG, "Error message: " + error.getMessage());
                Log.d(TAG, "Error details: " + error.getDetails());
                // TODO: Log real error to server.
            }

            @Override
            public void onSuccess(ChatRoom chatRoom) {
                ChatFragment prevFragment = instantiateItem(container, currentPosition);
                cleanChatRoom(currentChatRoom, prevFragment); // currentChatRoom is still old chat room.
                currentPosition = position;
                configureCurrentChatRoom(chatRoom, fragment);
                fragment.setChatRoom(chatRoom);
//                user.setCurrentRoomId(chatRoom.getId());
                listener.onSuccess(chatRoom);
            }
        });
        // Not connected to internet.
    }


    /**
     * This method is supposed to be called when the user navigates away from the chat room.
     * The chat room is cleaned of observers, current user, etc.
     *
     * @param fragment The fragment associated with the specified chat room.
     * @param chatRoom The chat room that is being left.
     */
    private void cleanChatRoom(ChatRoom chatRoom, ChatFragment fragment) {
        // Clean previous chat room.
        if (chatRoom != null) { // Not the first chat room in session.
            chatRoom.removeUser(user); // Remove user from chat room they just exited.
            if (fragment != null) {
                chatRoom.removeObserver(fragment);
            }
        }
    }

    /**
     * This method is meant to be called when the user enters a chat room.
     * The chat room is configured with observers, the user is added, etc.
     *
     * @param chatRoom The chat room being entered.
     * @param fragment The fragment associated with the chat room.
     */
    private void configureCurrentChatRoom(ChatRoom chatRoom, ChatFragment fragment) {
        currentChatRoom = chatRoom;// Set currentChatRoom to the chat room the user just entered.
        currentChatRoom.addObserver(fragment);
        currentChatRoom.addUser(user); // Add user to the chat room they just entered.
    }


    @Override
    public ChatFragment instantiateItem(ViewGroup container, int position) {
        return (ChatFragment) super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return 20;
    }

}
