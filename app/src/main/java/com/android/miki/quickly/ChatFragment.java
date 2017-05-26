package com.android.miki.quickly;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/25/2017.
 */

public class ChatFragment extends Fragment {


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
    private List<Message> messages;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mSendButton = (Button) view.findViewById(R.id.send_button);
        mMessageBox = (EditText) view.findViewById(R.id.message_box);
        chatRoom = (ChatRoom) getArguments().getSerializable("chatRoom");
        messages = new ArrayList<>();


        // Retrieve messages from Firebase.

        mMessagesRef.child(chatRoom.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot messageList) {
                for (DataSnapshot child : messageList.getChildren()) {
                    Message message = child.getValue(Message.class);
                    messages.add(message);
                }

                // Initialize message view (RecyclerView), after messages have been retrieved.
                mRecyclerView = (RecyclerView) view.findViewById(R.id.messages_recycler_view);
                mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
                ;
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new ChatRecyclerAdapter(messages);
                mRecyclerView.setAdapter(mAdapter);
                VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(10); // 10dp
                mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
                scrollToBottom();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    mMessagesRef.child(chatRoom.getChatId()).push().setValue(outgoingMessage);
                    mAvailableChatsRef.child(chatRoom.getChatId()).child("lastMessage").setValue(outgoingMessage);
                    int lastIndex = mAdapter.insertMessage(outgoingMessage);
                    mAdapter.notifyItemInserted(lastIndex);
                    mMessageBox.setText("");
                } else {
                    Toast.makeText(view.getContext(), "No text in message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    /**
     * Scrolls to the most recent message.
     */
    public void scrollToBottom() {
        if (mRecyclerView != null) { // Only do this if the view for the fragment has already been created.
            int messagesSize = (messages.size() - 1 < 0) ? 0 : messages.size() - 1;
            mRecyclerView.scrollToPosition(messagesSize);
        }
    }






}
