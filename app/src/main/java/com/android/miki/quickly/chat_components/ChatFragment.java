package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.group_info.GroupInfoActivity;
import com.android.miki.quickly.utilities.VerticalSpaceItemDecoration;
import com.android.miki.quickly.gif_drawer.GifDrawer;
import com.android.miki.quickly.gif_drawer.GifDrawerAction;
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

import static android.app.Activity.RESULT_OK;

/**
 * Created by mpokr on 5/25/2017.
 */

public class ChatFragment extends Fragment implements ChatRoomObserver {


    private Button mSendButton;
    private EditText mMessageEditText;
    private ImageButton gifButton;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mAvailableChatsRef = mRootRef.child("availableChats");
    private DatabaseReference mMessagesRef = mRootRef.child("messages");
    private RecyclerView mMessagesRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatRecyclerAdapter mAdapter;
    private GifDrawer mGifDrawer;
    private ChatRoom chatRoom;
    private User user;
    private List<Message> messages;
    private boolean isGifDrawerOpen;
    private static final String TAG = "ChatFragment";
    private static final int GIF_KEYBOARD_SHIFT = 500; // 500 pixels
    public static int GROUP_INFO_REQUEST_CODE = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setHasOptionsMenu(true);
        mSendButton = (Button) view.findViewById(R.id.send_button);
        mMessageEditText = (EditText) view.findViewById(R.id.message_box);
        gifButton = (ImageButton) view.findViewById(R.id.gif_button);
        mMessagesRecyclerView = (RecyclerView) view.findViewById(R.id.gif_recycler_view);
        chatRoom = (ChatRoom) getArguments().getSerializable(getString(R.string.chat_room));
        user = (User) getArguments().getSerializable("user");
        messages = new ArrayList<>();
        mGifDrawer = new GifDrawer(view, chatRoom, user, new GifDrawerAction() {
            @Override
            public void gifSent(Message message) {
                int lastIndex = mAdapter.insertMessage(message);
                mAdapter.notifyItemInserted(lastIndex);
                closeGifDrawer();
            }
        });
        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGifDrawerOpen) {
                    closeGifDrawer();
                } else {
                    openGifDrawer();
                }
            }
        });


        // Retrieve messages from Firebase.
        mMessagesRef.child(chatRoom.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot messageList) {
                for (DataSnapshot child : messageList.getChildren()) {
                    Message message = child.getValue(Message.class);
                    messages.add(message);
                }

                // Initialize message view (RecyclerView), after messages have been retrieved.
                mMessagesRecyclerView = (RecyclerView) view.findViewById(R.id.messages_recycler_view);
                mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
                mMessagesRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new ChatRecyclerAdapter(chatRoom, messages, user, (ChatSelectionActivity) getActivity());
                mMessagesRecyclerView.setAdapter(mAdapter);
                mMessagesRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });
                // Set spaces between messages
                VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(20); // 20dp
                mMessagesRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
                scrollToBottom();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            private int oldNumLines = 1;
            private long lastEdited;
            private Thread thread;
            private String query;
            RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.message_box_layout);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) rl.getLayoutParams();
            private int originalMessageBoxHeight = llp.height;
            private int typingInterval = 600;   // 600 ms
            private Runnable runnableTextWatcher = new Runnable() {
                @Override
                public void run() {
                    //
                    while (true) {
                        if ((System.currentTimeMillis() - lastEdited) > typingInterval) {
                            getGifs(query);
                            thread = null;
                            break;
                        }
                    }
                }
            };

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                query = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Paint textPaint = mMessageEditText.getPaint();
                Rect bounds = new Rect();
                String text = editable.toString();
                textPaint.getTextBounds(text, 0, text.length(), bounds);
                int textHeight = bounds.height() * mMessageEditText.getLineCount();
                Log.d(TAG, "text height: " + textHeight);
                RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.message_box_layout);
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) rl.getLayoutParams();
                int paddingAndMargins = mMessageEditText.getPaddingBottom() + mMessageEditText.getPaddingTop() + llp.bottomMargin + llp.topMargin;
                int editTextHeight = mMessageEditText.getHeight() - paddingAndMargins;
                Log.d(TAG, "edittext height: " + editTextHeight);
                Log.d(TAG, "padding&margins: " + paddingAndMargins);

                int currentLineCount = mMessageEditText.getLineCount();
                if (oldNumLines < currentLineCount) {
                    llp.height += (currentLineCount - oldNumLines) * paddingAndMargins * 1.5;
                    rl.setLayoutParams(llp);
                    oldNumLines = currentLineCount;
                } else if (oldNumLines > currentLineCount) {
                    llp.height -= (oldNumLines - currentLineCount) * paddingAndMargins * 1.5;
                    rl.setLayoutParams(llp);
                    oldNumLines = currentLineCount;
                }
                Log.d(TAG, "" + (editTextHeight - textHeight));

                if (text.equals("")) {
                    llp.height = originalMessageBoxHeight;
                    rl.setLayoutParams(llp);
                    Log.d(TAG, "back to original");
                }

                if (isGifDrawerOpen) {
                    lastEdited = System.currentTimeMillis();
                    if (thread == null) {
                        thread = new Thread(runnableTextWatcher);
                        thread.start();
                    }
                }
            }
        });


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isMessageEmpty = TextUtils.isEmpty(mMessageEditText.getEditableText().toString());
                if (!isMessageEmpty) {
                    String messageText = mMessageEditText.getText().toString();
                    Message outgoingMessage = new Message(System.currentTimeMillis(), user, messageText);
                    chatRoom.addMessage(outgoingMessage);
                    int lastIndex = mAdapter.insertMessage(outgoingMessage);
                    mAdapter.notifyItemInserted(lastIndex);
                    mMessageEditText.setText("");
                    scrollToBottom();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
        if (mMessagesRecyclerView != null) { // Only do this if the view for the fragment has already been created.
            int messagesSize = (messages.size() - 1 < 0) ? 0 : messages.size() - 1;
            mMessagesRecyclerView.scrollToPosition(messagesSize);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_chat_overflow_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_info:
                Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                intent.putExtra(getString(R.string.chat_room), chatRoom);
                startActivityForResult(intent, GROUP_INFO_REQUEST_CODE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GROUP_INFO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                chatRoom = (ChatRoom) data.getSerializableExtra((getString(R.string.chat_room)));
            }
        }
    }

    private void closeGifDrawer() {
        mMessageEditText.setHint(R.string.message_box_hint);
        gifButton.setImageResource(R.mipmap.gif_icon);
        isGifDrawerOpen = false;
        mGifDrawer.setGone();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMessagesRecyclerView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin - GIF_KEYBOARD_SHIFT);
        mMessagesRecyclerView.setLayoutParams(layoutParams);
        scrollToBottom();
    }

    private void openGifDrawer() {
        mMessageEditText.setHint("Search GIPHY...");
        gifButton.setImageResource(R.drawable.ic_close_black_24dp);
        isGifDrawerOpen = true;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMessagesRecyclerView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin + GIF_KEYBOARD_SHIFT);
        mMessagesRecyclerView.setLayoutParams(layoutParams);
        scrollToBottom();
        mGifDrawer.getTrendingGifs();
    }

    private void getGifs(String query) {
        if (query.equals("")) {
            mGifDrawer.getTrendingGifs();
        } else {
            mGifDrawer.translateTextToGif(query);
        }
    }

    @Override
    public void numUsersChanged(int numUsers) {

    }

    @Override
    public void userAdded(User user) {

    }

    @Override
    public void userRemoved(User user) {

    }

    @Override
    public void nameChanged(String name) {

    }

    @Override
    public void messageAdded(Message message) {

    }
}
