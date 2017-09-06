package com.android.miki.quickly.group_info;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.miki.quickly.R;
import com.android.miki.quickly.core.FirebaseActivity;
import com.android.miki.quickly.core.network.FirebaseClient;
import com.android.miki.quickly.firebase_requests.chatroom_model_requests.ChangeGroupNameRequest;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.ui.VerticalSpaceItemDecoration;
import com.android.miki.quickly.utils.DialogBuilderHelper;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;

/**
 * Created by mpokr on 6/17/2017.
 */

public class GroupInfoActivity extends FirebaseActivity {

    private RecyclerView mRecyclerView;
    private GroupInfoRecyclerAdapter mAdapter;
    private ChatRoom chatRoom;
    private boolean isDialogOpen;
    private GroupNameDialogListener dialogListener;
    private String dialogText;
    private MaterialDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatRoom = (ChatRoom) getIntent().getSerializableExtra(getString(R.string.chat_room));
        setContent(R.layout.activity_group_info);
        final Toolbar actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mRecyclerView = findViewById(R.id.group_info_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        initializeDialogListener();
        mAdapter = new GroupInfoRecyclerAdapter(this, chatRoom, dialogListener);
        VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(50); // 50dp
        mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setState(SUCCESS);
    }

    private void initializeDialogListener() {
        dialogListener = new GroupNameDialogListener() {

            @Override
            public void editGroupNameRequested() {
                makeDialog(chatRoom.getName());
            }
        };
    }

    private void makeDialog(String dialogPrefill) {
        String dialogHint = getResources().getString(R.string.change_group_name);
        int lightBlue = ContextCompat.getColor(this, R.color.LightBlue);
        dialog = new DialogBuilderHelper().inputDialog(this, lightBlue)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .input(dialogHint, dialogPrefill, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input) {
                        final String newName = input.toString();
                        ChangeGroupNameRequest changeGroupNameQuery = new ChangeGroupNameRequest(chatRoom.getId(), newName);
                        FirebaseClient client = FirebaseClient.getInstance();
                        client.execute(changeGroupNameQuery, new FirebaseListener<Void>() {
                            @Override
                            public void onLoading() {
                                setState(LOADING);
                            }

                            @Override
                            public void onError(FirebaseError error) {
                                Toast.makeText(GroupInfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(Void nothing) {
                                chatRoom.changeName(newName);
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        isDialogOpen = false;
                        GroupInfoActivity.this.dialogText = null;
                        GroupInfoActivity.this.dialog = null;
                    }
                })
                .build();
        if (!this.isFinishing()) {
            showDialog();

        }
    }

    private void showDialog() {
        final EditText inputEditText = dialog.getInputEditText();
        if (inputEditText != null) {
            this.dialogText = inputEditText.getText().toString();
        }
        dialog.show();
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
            if (dialog != null) {
                EditText inputEditText = dialog.getInputEditText();
                if (inputEditText != null) {
                    dialogText = inputEditText.getText().toString();
                }
                getIntent().putExtra(getString(R.string.dialog_text), dialogText);
                getIntent().putExtra(getString(R.string.is_dialog_open), isDialogOpen);
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDialogOpen = getIntent().getBooleanExtra(getString(R.string.is_dialog_open), false);
        if (isDialogOpen) {
            dialogText = getIntent().getStringExtra(getString(R.string.dialog_text));
            makeDialog(dialogText);
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
