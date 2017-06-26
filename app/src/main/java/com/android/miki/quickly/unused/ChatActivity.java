package com.android.miki.quickly.unused;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.miki.quickly.chat_components.ChatRecyclerAdapter;
import com.android.miki.quickly.R;
import com.android.miki.quickly.utilities.VerticalSpaceItemDecoration;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private Button mSendButton;
    private EditText mMessageBox;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mAvailableChatsRef = mRootRef.child("availableChats");
    private DatabaseReference mMessagesRef = mRootRef.child("messages");
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatRecyclerAdapter mAdapter;

    private ChatRoom chatRoom;
    private User user;
    private List<Message> messages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        mSendButton = (Button) findViewById(R.id.send_button);
        mMessageBox = (EditText) findViewById(R.id.message_box);
        chatRoom = (ChatRoom) getIntent().getSerializableExtra("chatRoom");
        messages = new ArrayList<>();
        /*
        Retrieve messages from Firebase.
         */
        mMessagesRef.child(chatRoom.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot messageList) {
                for (DataSnapshot child : messageList.getChildren()) {
                    Message message = child.getValue(Message.class);
                    messages.add(message);
                }

                // Initialize message view (RecyclerView), after messages have been retrieved.
                mRecyclerView = (RecyclerView) findViewById(R.id.messages_recycler_view);
                mLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
                ;
                mRecyclerView.setLayoutManager(mLayoutManager);
                SnapHelper snapHelper = new LinearSnapHelper();
                mRecyclerView.setOnFlingListener(null);
                snapHelper.attachToRecyclerView(mRecyclerView);
                mAdapter = new ChatRecyclerAdapter(messages, u);
                mRecyclerView.setAdapter(mAdapter);
                VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(10); // 10dp
                mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
                // Make sure messagesSize isn't negative
                final int messagesSize = (messages.size() - 1 < 0) ? 0 : messages.size() - 1;
                mRecyclerView.scrollToPosition(messagesSize);
                mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (bottom < oldBottom) {
                            mRecyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mRecyclerView.scrollToPosition(messagesSize);
                                }
                            }, 0);
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isMessageEmpty = TextUtils.isEmpty(mMessageBox.getEditableText().toString());
                if (!isMessageEmpty) {
                    String sender = "John";
                    String messageText = mMessageBox.getText().toString();
                    Message outgoingMessage = new Message(System.currentTimeMillis(), sender, messageText);
                    mMessagesRef.child(chatRoom.getId()).push().setValue(outgoingMessage);
                    mAvailableChatsRef.child(chatRoom.getId()).child("lastMessage").setValue(outgoingMessage);
                    int lastIndex = mAdapter.insertMessage(outgoingMessage);
                    mAdapter.notifyItemInserted(lastIndex);
                    mMessageBox.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "No text in message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
