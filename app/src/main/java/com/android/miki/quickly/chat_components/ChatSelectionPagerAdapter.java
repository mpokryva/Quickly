package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.core.chat_room.ChatRoomManager;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionPagerAdapter extends FragmentStatePagerAdapter implements ConnectivityStatusObserver {

    private ChatRoomManager chatRoomManager;
    private User user;
    private ChatRoom chatRoom;
    private ChatRoom currentChatRoom;
    private FragmentManager fm;
    private static final String TAG = ChatSelectionPagerAdapter.class.getName();
    private int currentPosition;
    private Context mContext;
    private boolean isConnected;
    private ChatFragment currentFragment;
    private boolean shouldForceDisconnect;
    private CustomViewPager viewPager;


    public ChatSelectionPagerAdapter(FragmentManager fm, User user, Context context) {
        super(fm);
        this.fm = fm;
        chatRoomManager = ChatRoomManager.getInstance();
        this.user = user;
        mContext = context;
        ConnectivityStatusNotifier notifier = ConnectivityStatusNotifier.getInstance();
        notifier.registerObserver(this);
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new ChatFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;

    }

    public void loadRoom(final CustomViewPager container, final int position) {
        viewPager = container;
        final ChatFragment fragment = (ChatFragment) instantiateItem(container, position);
        if (position == container.getCurrentItem()) {
            currentFragment = fragment;
        }
        ConnectivityStatusNotifier notifier = ConnectivityStatusNotifier.getInstance();
        boolean isConnected = notifier.isConnected(container.getContext());
        Log.d(TAG, "isConnected from notifier: " + isConnected);
        if (isConnected) {
            chatRoomManager.getRoom(position, new FirebaseListener<ChatRoom>() {
                @Override
                public void onLoading() {
                    fragment.onLoading();
                }

                // Connected to internet (according to ConnectivityNotifer), but not able to complete request.
                @Override
                public void onError(FirebaseError error) {
                    Log.d(TAG, "Error message: " + error.getMessage());
                    Log.d(TAG, "Error details: " + error.getDetails());
                    forceDisconnect(container, currentPosition, FirebaseError.serverError());
                    // TODO: Log real error to server.
                }

                @Override
                public void onSuccess(ChatRoom chatRoom) {
                    if (!viewPager.isPagingEnabled()) {
                        viewPager.setPagingEnabled(true);
                    }
                    ChatSelectionPagerAdapter.this.chatRoom = chatRoom;
                    ChatFragment prevFragment = instantiateItem(container, currentPosition);
                    cleanChatRoom(currentChatRoom, prevFragment); // currentChatRoom is still old chat room.
                    currentPosition = position;
                    fragment.onSuccess(chatRoom);
                    configureCurrentChatRoom(chatRoom, fragment);

                }
            });
            // Not connected to internet.
        } else {
            // Current position didn't update yet. Still previous position.
            forceDisconnect(container, currentPosition, FirebaseError.serverError());
        }
    }

    /**
     * Triggers the onDisconnect() method, along with disabling swiping.
     *
     * @param viewPager
     * @param position
     */
    private void forceDisconnect(CustomViewPager viewPager, int position, FirebaseError error) {
        Log.d(TAG, "Force disconnect");
        shouldForceDisconnect = true;
        onDisconnect(error);
        shouldForceDisconnect = false;
        viewPager.setPagingEnabled(false);
        viewPager.setCurrentItem(position); // Position represents the previous position here.
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
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
                fragment.revertToStartState();
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
        //                actionBar.setTitle(getUserString(currentChatRoom));
    }


    @Override
    public ChatFragment instantiateItem(ViewGroup container, int position) {
        return (ChatFragment) super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return chatRoomManager.getItemsInCache();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public void onConnect() {
        if (!isConnected) {
            isConnected = true;
            if (viewPager != null) {
                loadRoom(viewPager, viewPager.getCurrentItem());
            } else {
                if (currentFragment != null) {
                    currentFragment.onConnect();
                    Log.d(TAG, "Fragment is handling loading.");
                }
            }
        }
    }

    @Override
    public void onDisconnect(FirebaseError error) {
        if (isConnected || shouldForceDisconnect) {
            isConnected = false;
            if (currentFragment != null) {
                currentFragment.onDisconnect(error);
            }
        }
    }

    @Override
    public Context retrieveContext() {
        return mContext;
    }


}
