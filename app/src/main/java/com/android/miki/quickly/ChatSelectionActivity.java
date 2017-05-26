package com.android.miki.quickly;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionActivity extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private List<ChatRoom> chatRooms;
    private ChatFinder mChatFinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        chatRooms = new ArrayList<>();
        mChatFinder = new ChatFinder();
        mChatFinder.findChatRoomCallback(new SimpleCallback<List<ChatRoom>>() {
            @Override
            public void callback(List<ChatRoom> cbChatRooms) {
                chatRooms = cbChatRooms;
                //mRecyclerView.setHasFixedSize(true);
                mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                mLayoutManager = new LinearLayoutManager(ChatSelectionActivity.this, LinearLayoutManager.HORIZONTAL, false);
                mRecyclerView.setLayoutManager(mLayoutManager);
                SnapHelper snapHelper = new LinearSnapHelper();
                mRecyclerView.setOnFlingListener(null);
                snapHelper.attachToRecyclerView(mRecyclerView);
                mAdapter = new ChatSelectionRecyclerAdapter(chatRooms);
                mRecyclerView.setAdapter(mAdapter);
            }
        });

    }
}
