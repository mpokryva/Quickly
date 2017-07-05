package com.android.miki.quickly.chat_components;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utilities.ChatRoomManager;
import com.android.miki.quickly.utilities.FragmentCallBack;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionPagerAdapter extends FragmentStatePagerAdapter {

    private ChatRoomManager chatRoomManager;
    private User user;


    public ChatSelectionPagerAdapter(FragmentManager fm, User user) {
        super(fm);
        chatRoomManager = ChatRoomManager.getInstance();
        this.user = user;
    }



    @Override
    public ChatFragment getItem(int position) {
        chatRoomManager.setPosition(position);
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        // Pass chat room info to fragment
        String chatRoomString = "chatRoom";
        //args.putSerializable(chatRoomString, room);
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public ChatFragment instantiateItem(ViewGroup container, int position) {
        return (ChatFragment) super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return chatRoomManager.getItemsInCache();
    }


}
