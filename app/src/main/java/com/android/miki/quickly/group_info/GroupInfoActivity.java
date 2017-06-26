package com.android.miki.quickly.group_info;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
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
        final Toolbar actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mRecyclerView = (RecyclerView) findViewById(R.id.group_info_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new GroupInfoRecyclerAdapter(chatRoom, new GroupNameDialogListener() {
            @Override
            public void groupNameChanged(String newGroupName) {
                Toast.makeText(GroupInfoActivity.this, newGroupName, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void editGroupNameRequested() {
                GroupNameDialogFragment dialogFragment = new GroupNameDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable(GroupNameDialogListener.TAG, this);
                dialogFragment.setArguments(args);
                dialogFragment.show(getSupportFragmentManager(), GroupNameDialogFragment.TAG);
            }
        });
        VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(50); // 50dp
        mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
