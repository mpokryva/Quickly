package com.android.miki.quickly.chat_components;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.recyclerview_adapters.GroupInfoRecyclerAdapter;
import com.android.miki.quickly.utilities.VerticalSpaceItemDecoration;

/**
 * Created by mpokr on 6/17/2017.
 */

public class GroupInfoActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private GroupInfoRecyclerAdapter mAdapter;
    private ChatRoom chatRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRoom = (ChatRoom) getIntent().getSerializableExtra("chatRoom");
        setContentView(R.layout.activity_group_info);
        mRecyclerView = (RecyclerView) findViewById(R.id.group_info_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new GroupInfoRecyclerAdapter(chatRoom);
        VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(50); // 50dp
        mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }
}
