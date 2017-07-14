package com.android.miki.quickly.chat_components;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.android.miki.quickly.core.ErrorFragment;
import com.android.miki.quickly.core.LoadingFragment;
import com.android.miki.quickly.core.Status;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utilities.ChatRoomManager;
import com.android.miki.quickly.utilities.FirebaseListener;
import com.google.firebase.database.DatabaseError;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionPagerAdapter extends FragmentStatePagerAdapter {

    private ChatRoomManager chatRoomManager;
    private User user;
    private ChatRoom chatRoom;
    private Status status;
    private ChatRoom currentChatRoom;
    private ConcurrentHashMap<Integer, Status> positionToTypeMap;
    private Iterator<Map.Entry<Integer, Status>> mapIterator;
    private FragmentManager fm;
    private static final String TAG = ChatSelectionPagerAdapter.class.getName();


    public ChatSelectionPagerAdapter(FragmentManager fm, User user) {
        super(fm);
        this.fm = fm;
        chatRoomManager = ChatRoomManager.getInstance();
        this.user = user;
        status = Status.LOADING;
        positionToTypeMap = new ConcurrentHashMap<>();
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new ChatFragment();

        return fragment;

    }

    public void loadRoom(final ViewPager container, final int position) {
        chatRoomManager.getRoom(position, new FirebaseListener<ChatRoom>() {
            @Override
            public void onLoading() {
                status = Status.LOADING;
                int currentPosition = container.getCurrentItem();
                ChatFragment fragment = (ChatFragment) instantiateItem(container, position);
                fragment.setLoading();
            }

            @Override
            public void onError(DatabaseError error) {
                status = Status.ERROR;
                ChatFragment fragment = (ChatFragment) instantiateItem(container, position);
                fragment.setError();
            }

            @Override
            public void onSuccess(ChatRoom chatRoom) {
                status = Status.SUCCESS;
                ChatSelectionPagerAdapter.this.chatRoom = chatRoom;
                ChatFragment fragment = (ChatFragment) instantiateItem(container, position);
                fragment.setSuccess(chatRoom, user);

                if (currentChatRoom != null) { // Not the first chat room in session.
                    currentChatRoom.removeUser(user); // Remove user from chat room they just exited.
                    currentChatRoom.removeObserver(fragment);
                }

                currentChatRoom = chatRoom;// Set currentChatRoom to the chat room the user just entered.
                currentChatRoom.addObserver(fragment);
                currentChatRoom.addUser(user); // Add user to the chat room they just entered.
//                actionBar.setTitle(getUserString(currentChatRoom));
                fragment.scrollToBottom(); // Scroll to bottom (last message in chat).
            }
        });
    }

    @Override
    public int getItemPosition(Object object) {
      return super.getItemPosition(object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);
        positionToTypeMap.put(position, status);
        return item;
    }

    @Override
    public int getCount() {
        return chatRoomManager.getItemsInCache();
    }

    private void clearFragments(ViewGroup container) {
        for (Integer position : positionToTypeMap.keySet()) {
            Status type = positionToTypeMap.get(position);
            Object fragment = instantiateItem(container, position);
            if (fragment instanceof LoadingFragment || fragment instanceof ErrorFragment) {
                destroyItem(container, position, fragment);
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        positionToTypeMap.remove(position);
        super.destroyItem(container, position, object);
    }
}
