package com.android.miki.quickly.chat_components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import android.view.ViewStub;
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
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.group_info.GroupInfoActivity;
import com.android.miki.quickly.ui.AnimatorUtil;
import com.android.miki.quickly.ui.GrowingAnimation;
import com.android.miki.quickly.ui.ShrinkingAnimation;
import com.android.miki.quickly.utils.FirebaseError;
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

public class ChatFragment extends Fragment implements ChatRoomObserver, ConnectivityStatusObserver {

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mMessagesRef = mRootRef.child("messages");
    private RecyclerView mMessagesRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatRecyclerAdapter mAdapter;
    private ChatRoom chatRoom;
    private User user;
    private List<Message> messages;
    private boolean isGifDrawerOpen;
    private static final String TAG = ChatFragment.class.getName();
    private static final int GIF_KEYBOARD_SHIFT = 500; // 500 pixels
    public static int GROUP_INFO_REQUEST_CODE = 0;
    private boolean isConnected;
    private int position;
    private ProgressBar progressWheel;
    private View content;
    private View loadingView;
    private View errorView;
    private TextView errorMessage;
    private TextView errorDetais;
    public static final int SUCCESS = 1;
    public static final int ERROR = -1;
    public static final int LOADING = 2;
    private int state;

    private boolean firstComponentInitialization = true;
    private ActionBarListener callbackToActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isConnected = true; // Initialized to true to avoid double loading of chat room initially.
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        state = LOADING;
        loadingView = view.findViewById(R.id.loading_view);
        progressWheel = loadingView.findViewById(R.id.progress_wheel);
        int lightBlue = ContextCompat.getColor(getContext(), R.color.LightBlue); // Color the progress whel light blue.
        progressWheel.getIndeterminateDrawable().setColorFilter(lightBlue, PorterDuff.Mode.MULTIPLY);
        errorView = view.findViewById(R.id.error_view);
        errorMessage = errorView.findViewById(R.id.error_message);
        errorDetais = errorView.findViewById(R.id.error_details);
        content = view.findViewById(R.id.content);

        setHasOptionsMenu(true);
        mMessagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        // TODO: Handle null user/position?
        user = (User) getArguments().getSerializable("user");
        position = getArguments().getInt("position");
        messages = new ArrayList<>();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                mAdapter = new ChatRecyclerAdapter(chatRoom, messages, (ChatSelectionActivity) getActivity());
                mMessagesRecyclerView.setAdapter(mAdapter);
                mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
                mMessagesRecyclerView.setLayoutManager(mLayoutManager);
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
                scrollToBottom();
                setState(SUCCESS);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null && state != SUCCESS) {
            setState(LOADING);
        }
    }

    /**
     * Use this method to change state.
     * DO NOT CHANGE STATE DIRECTLY!
     *
     * @param state The new state to set.
     */
    public void setState(int state) {
        View view = getView();
        if (view != null) {
            if (this.state != state) {
                View viewToHide;
                switch (this.state) {
                    case LOADING:
                        viewToHide = loadingView;
                        break;
                    case SUCCESS:
                        viewToHide = this.content;
                        break;
                    case ERROR:
                        viewToHide = errorView;
                        break;
                    default:
                        throw new IllegalArgumentException("OLD Status must be either LOADING, SUCCESS, or ERROR.");
                }
                viewToHide.setVisibility(View.GONE);
                View viewToShow;
                this.state = state; // Update state.
                switch (this.state) {
                    case LOADING:
                        viewToShow = view.findViewById(R.id.loading_view);
                        break;
                    case SUCCESS:
                        viewToShow = this.content;
                        break;
                    case ERROR:
                        viewToShow = view.findViewById(R.id.error_view);
                        break;
                    default:
                        throw new IllegalArgumentException("NEW Status must be either LOADING, SUCCESS, or ERROR.");
                }
                viewToShow.setVisibility(View.VISIBLE);
            }
        } else {
            throw new IllegalStateException("View in FirebaseFragment should not be null");
        }

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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void numUsersChanged(int numUsers) {

    }

    @Override
    public void userAdded(User user) {
        View view = getView();
        if (view != null) {
            View snackbarLayout = view.findViewById(R.id.fragment_chat_coordinator_wrapper);
            if (snackbarLayout != null) {
                Snackbar snackbar = Snackbar.make(snackbarLayout, user.getDisplayName() + " has joined!", Snackbar.LENGTH_SHORT);
                int color = ContextCompat.getColor(getContext(), R.color.LightBlue);
                snackbar.getView().setBackgroundColor(color);
                snackbar.show();
            }
        }

    }

    @Override
    public void userRemoved(User user) {

    }

    @Override
    public void nameChanged(String name) {

    }

    @Override
    public void messageAdded(Message message) {
        int lastIndex = mAdapter.insertMessage(message);
        mAdapter.notifyItemInserted(lastIndex);
        scrollToBottom();
    }

    @Override
    public void onConnect() {
        if (!isConnected) {
            isConnected = true;
            updateUI(true);
        }
    }

    private void updateUI(boolean isConnected) {

    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        initComponents();
    }

    @Override
    public void onDisconnect(FirebaseError error) {
        if (isConnected) {
            isConnected = false;
            updateUI(false);
        }
    }

    public void onGifDrawerOpened() {
//        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMessagesRecyclerView.getLayoutParams();
//        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin + GIF_KEYBOARD_SHIFT);
//        mMessagesRecyclerView.setLayoutParams(layoutParams);
//        scrollToBottom();
    }
    public void onGifDrawerClosed() {
//        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mMessagesRecyclerView.getLayoutParams();
//        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin - GIF_KEYBOARD_SHIFT);
//        mMessagesRecyclerView.setLayoutParams(layoutParams);
//        scrollToBottom();
    }

}