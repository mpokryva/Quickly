package com.android.miki.quickly.chat_components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.core.FirebaseFragment;
import com.android.miki.quickly.core.chat_room.ChatRoomManager;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.group_info.GroupInfoActivity;
import com.android.miki.quickly.login_signup.LoginListener;
import com.android.miki.quickly.ui.AnimatorUtil;
import com.android.miki.quickly.ui.GrowingAnimation;
import com.android.miki.quickly.ui.ShrinkingAnimation;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.android.miki.quickly.ui.VerticalSpaceItemDecoration;
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

public class ChatFragment extends FirebaseFragment<ChatRoom> implements ChatRoomObserver, ConnectivityStatusObserver {

    private ImageButton mSendButton;
    private EditText mMessageEditText;
    private Button gifButton;
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
    private ProgressBar progressWheel;
    private View content;
    private View loadingView;
    private View errorView;
    private TextView errorMessage;
    private TextView errorDetais;
    private boolean isConnected;
    private int position;
    private boolean firstComponentInitialization = true;
    private ActionBarListener callbackToActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isConnected = true; // Initialized to true to avoid double loading of chat room initially.
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        loadingView = view.findViewById(R.id.loading_view);
        progressWheel = (ProgressBar) loadingView.findViewById(R.id.progress_wheel);
        int lightBlue = ContextCompat.getColor(getContext(), R.color.LightBlue); // Color the progress whel light blue.
        progressWheel.getIndeterminateDrawable().setColorFilter(lightBlue, PorterDuff.Mode.MULTIPLY);
        errorView = view.findViewById(R.id.error_view);
        errorMessage = (TextView) errorView.findViewById(R.id.error_message);
        errorDetais = (TextView) errorView.findViewById(R.id.error_details);
        setHasOptionsMenu(true);
        content = view.findViewById(R.id.content);
        mSendButton = (ImageButton) view.findViewById(R.id.send_button);
        mMessageEditText = (EditText) view.findViewById(R.id.message_box);
        gifButton = (Button) view.findViewById(R.id.gif_button);
        mMessagesRecyclerView = (RecyclerView) view.findViewById(R.id.messages_recycler_view);
        // TODO: Handle null chatRoom?
        user = (User) getArguments().getSerializable("user");
        position = getArguments().getInt("position");
        messages = new ArrayList<>();
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

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                callbackToActivity = (ActionBarListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement ActionBarListener");
            }
        } else {
            Log.d(TAG, "Context in onAttach() is not an activity.");
        }
    }

    private void initComponents() {
        final View view = ChatFragment.this.getView();
        String roomName = (chatRoom.getName() != null) ? chatRoom.getName() : chatRoom.getDefaultName();
        callbackToActivity.setTitle(roomName);
        mGifDrawer = new GifDrawer(view, chatRoom, user, new GifDrawerAction() {
            @Override
            public void gifSent(Message message) {
                int lastIndex = mAdapter.insertMessage(message);
                mAdapter.notifyItemInserted(lastIndex);
                closeGifDrawer();
            }
        });

        // Retrieve messages from Firebase.
        messages.clear();
        mMessagesRef.child(chatRoom.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot messageList) {
                for (DataSnapshot child : messageList.getChildren()) {
                    Message message = child.getValue(Message.class);
                    messages.add(message);
                }
                if (view == null) {
                    Log.d(TAG, "View in ChatFragment is null");
                    return;
                }
                // Initialize message view (RecyclerView), after messages have been retrieved.
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
                if (firstComponentInitialization) {
                    mMessagesRecyclerView.addItemDecoration(verticalSpaceItemDecoration);
                }
                firstComponentInitialization = false;
                content.setVisibility(View.VISIBLE);
                scrollToBottom();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    clearMessageBox();
                    scrollToBottom();
                    hideKeyboard(view);
                } else {
                    Toast.makeText(view.getContext(), "No text in message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                // Refresh button case handled in ChatSelectionActivity
        }
        return super.onOptionsItemSelected(item);
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

    public void revertToStartState() {
        closeGifDrawer();
        Log.d(TAG, "Reverting to start state");
        View view = getView();
        if (view != null) {
            hideKeyboard(view);
        }
    }

    private void clearMessageBox() {
        if (mMessageEditText != null) {
            mMessageEditText.setText("");
        }
    }

    private void toggleGifButton() {
        final int iconRes;
        if (isGifDrawerOpen) {
            iconRes = R.drawable.gif_icon;
        } else {
            iconRes = R.drawable.ic_close_black_24dp;
        }
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AnimatorUtil.on(gifButton).with(new GrowingAnimation()).duration(0).animate();
                Drawable newIcon = ContextCompat.getDrawable(ChatFragment.this.getContext(), iconRes);
                if (iconRes == R.drawable.ic_close_black_24dp) {
                    gifButton.setText("");
                } else {
                    gifButton.setText("GIF");
                }
                gifButton.setBackground(newIcon);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        AnimatorUtil.on(gifButton).with(new ShrinkingAnimation()).duration(200).listener(listener).animate();
    }

    private void toggleSendButton() {
        Animation animation;
        if (isGifDrawerOpen) {
            animation = new GrowingAnimation();
        } else {
            animation = new ShrinkingAnimation();
        }
        AnimatorUtil.on(mSendButton).with(animation).duration(200).animate();
    }

    private void closeGifDrawer() {
        if (isGifDrawerOpen) {
            toggleGifButton();
            toggleSendButton();
            hideGifDrawer();
        }
    }

    /**
     * Hide the gif drawer itself, and return components to their appropriate states.
     */
    private void hideGifDrawer() {
        mMessageEditText.setHint(R.string.message_box_hint);
        mSendButton.setVisibility(View.VISIBLE);
        isGifDrawerOpen = false;
        mGifDrawer.setShouldShow(false);
        // TODO: Actually implement this method: mGifDrawer.cancelGifRequests();
        mGifDrawer.hide();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMessagesRecyclerView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin - GIF_KEYBOARD_SHIFT);
        mMessagesRecyclerView.setLayoutParams(layoutParams);
        clearMessageBox();
        scrollToBottom();
    }

    private void openGifDrawer() {
        if (!isGifDrawerOpen) {
            toggleGifButton();
            toggleSendButton();
            showGifDrawer();
        }
    }

    private void showGifDrawer() {
        clearMessageBox();
        mMessageEditText.setHint(R.string.search_giphy);
        mSendButton.setVisibility(View.GONE);
        isGifDrawerOpen = true;
        mGifDrawer.setShouldShow(true);
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onSuccess(ChatRoom chatRoom) {
        super.onSuccess(chatRoom);
        this.chatRoom = chatRoom;
        initComponents();
    }

    @Override
    public void onLoading() {
        content.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(FirebaseError error) {
        content.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorMessage.setText(error.getMessage());
        errorDetais.setText(error.getDetails());
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMainContent() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideMainLayout() {
        content.setVisibility(View.GONE);
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

    @Override
    public void onConnect() {
        if (!isConnected) {
            isConnected = true;
            loadRoom();
        }
    }

    private void loadRoom() {
        ChatRoomManager.getInstance().getRoom(position, new FirebaseListener<ChatRoom>() {
            @Override
            public void onLoading() {
                ChatFragment.this.onLoading();
            }

            @Override
            public void onError(FirebaseError error) {
                ChatFragment.this.onError(error);
            }

            @Override
            public void onSuccess(ChatRoom chatRoom) {
                ChatFragment.this.onSuccess(chatRoom);
            }
        });
    }

    @Override
    public void onDisconnect(FirebaseError error) {
        isConnected = false;
        onError(error);
    }

    @Override
    public Context retrieveContext() {
        return getContext();
    }
}