package com.android.miki.quickly.chat_components;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;

import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionPagerAdapter extends FragmentStatePagerAdapter {

    private List<ChatRoom> chatRooms;
    private User user;


    public ChatSelectionPagerAdapter(FragmentManager fm, List<ChatRoom> chatRooms, User user) {
        super(fm);
        this.chatRooms = chatRooms;
        this.user = user;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        // Pass chat room info to fragment
        args.putSerializable("chatRoom", chatRooms.get(position));
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getCount() {
        return chatRooms.size();
    }


}
