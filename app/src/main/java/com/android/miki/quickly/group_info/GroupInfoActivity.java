package com.android.miki.quickly.group_info;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.miki.quickly.R;
import com.android.miki.quickly.chat_components.ChatFragment;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.utilities.VerticalSpaceItemDecoration;

/**
 * Created by mpokr on 6/17/2017.
 */

public class GroupInfoActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private GroupInfoRecyclerAdapter mAdapter;
    private ChatRoom chatRoom;
    private boolean isDialogOpen;
    private GroupNameDialogFragment dialogFragment;
    private GroupNameDialogListener dialogListener;
    private String dialogText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRoom = (ChatRoom) getIntent().getSerializableExtra(getString(R.string.chat_room));
        setContentView(R.layout.activity_group_info);
        final Toolbar actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mRecyclerView = (RecyclerView) findViewById(R.id.group_info_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        initializeDialogListener();
        mAdapter = new GroupInfoRecyclerAdapter(chatRoom, dialogListener);
        VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(50); // 50dp
        mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initializeDialogListener() {
        dialogListener = new GroupNameDialogListener() {
            @Override
            public void groupNameChanged(String newGroupName) {
                chatRoom.changeName(newGroupName);

            }

            @Override
            public void editGroupNameRequested() {
                makeDialog();
            }

            @Override
            public void dialogClosed() {
                isDialogOpen = false;
                dialogText = null;
            }
        };
    }

    private void makeDialog() {
        dialogFragment = new GroupNameDialogFragment();
        dialogFragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putSerializable(GroupNameDialogListener.TAG, dialogListener);
        if (dialogText != null) {
            args.putString(getString(R.string.dialog_text), dialogText);
        }
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), GroupNameDialogFragment.TAG);
        isDialogOpen = true;
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

    @Override
    protected void onPause() {
        super.onPause();
        if (isDialogOpen) {
            if (dialogFragment != null) {
                dialogText = dialogFragment.getText();
                getIntent().putExtra(getString(R.string.dialog_text), dialogText);
                getIntent().putExtra("isDialogOpen", isDialogOpen);
                dialogFragment.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDialogOpen = getIntent().getBooleanExtra("isDialogOpen", false);
        if (isDialogOpen) {
            dialogText = getIntent().getStringExtra(getString(R.string.dialog_text));
            makeDialog();
        }
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(getString(R.string.chat_room), chatRoom);
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }
}
