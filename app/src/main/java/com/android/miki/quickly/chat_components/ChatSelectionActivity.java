package com.android.miki.quickly.chat_components;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.android.miki.quickly.R;
import com.android.miki.quickly.core.FirebaseActivity;
import com.android.miki.quickly.core.Status;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utilities.ChatRoomManager;
import com.android.miki.quickly.utilities.FirebaseListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionActivity extends FirebaseActivity {

    private ViewPager mViewPager;
    private ChatSelectionPagerAdapter mAdapter;
    private List<ChatRoom> chatRooms;
    private ChatRoomManager roomManager;
    private User user;
    private ChatRoom currentChatRoom;
    private Toolbar actionBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        this.user = (User) getIntent().getSerializableExtra("user");
        chatRooms = new ArrayList<>();
        roomManager = ChatRoomManager.getInstance();

        mViewPager = (ViewPager) findViewById(R.id.chat_selection_pager);
        mAdapter = new ChatSelectionPagerAdapter(getSupportFragmentManager(), user);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(30);
        final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                mAdapter.loadRoom(mViewPager, position);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mViewPager.addOnPageChangeListener(pageChangeListener);
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(mViewPager.getCurrentItem());
            }
        });
    }

    private String getUserString(ChatRoom chatRoom) {
        String userString = "";
        Iterator<User> it = chatRoom.getUsers().values().iterator();
        while (it.hasNext()) {
            User user = it.next();
            userString += user.getNickname();
            if (it.hasNext()) {
                userString += ", ";
            }
        }

        return userString;
    }

    private void configureActionBar() {

    }
}
