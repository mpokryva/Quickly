package com.android.miki.quickly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ChatSelectionPagerAdapter mAdapter;
    private List<ChatRoom> chatRooms;
    private ChatFinder mChatFinder;
    private User user;
    private ChatRoom currentChatRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        Toolbar actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        this.user = (User) getIntent().getSerializableExtra("user");
        chatRooms = new ArrayList<>();
        mChatFinder = new ChatFinder();
        mChatFinder.findChatRoomCallback(new SimpleCallback<List<ChatRoom>>() {
            @Override
            public void callback(List<ChatRoom> cbChatRooms) {
                chatRooms = cbChatRooms;
                mViewPager = (ViewPager) findViewById(R.id.chat_selection_pager);
                mAdapter = new ChatSelectionPagerAdapter(getSupportFragmentManager(), chatRooms, user);
                mViewPager.setAdapter(mAdapter);
                mViewPager.setPageMargin(30);
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        ChatFragment fragment = (ChatFragment) mAdapter.instantiateItem(mViewPager, position);
                        if (currentChatRoom == null) {
                            currentChatRoom = chatRooms.get(position);
                        }
                        currentChatRoom.removeUser(user); // Remove user from chat room they just exited
                        currentChatRoom = chatRooms.get(position); // Set currentChatRoom to the chat room the user just entered
                        currentChatRoom.addUser(user); // Add user to the chat room they just entered
                        fragment.scrollToBottom(); // Scroll to bottom (last message in chat)

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        });

    }
}
