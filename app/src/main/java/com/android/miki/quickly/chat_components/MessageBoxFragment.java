package com.android.miki.quickly.chat_components;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.gif_drawer.GifDrawer;
import com.android.miki.quickly.gif_drawer.GifDrawerAction;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.ui.AnimatorUtil;
import com.android.miki.quickly.ui.GrowingAnimation;
import com.android.miki.quickly.ui.ShrinkingAnimation;
import com.android.miki.quickly.utils.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 8/23/2017.
 */

public class MessageBoxFragment extends Fragment implements ChatRoomObserver, ConnectivityStatusObserver {

    private ImageButton mSendButton;
    private EditText mMessageEditText;
    private Button gifButton;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mMessagesRef = mRootRef.child("messages");
    private GifDrawer gifDrawer;
    private ChatRoom chatRoom;
    private List<Message> messages;
    private boolean isGifDrawerOpen;
    private static final String TAG = ChatFragment.class.getName();
    private static final int GIF_KEYBOARD_SHIFT = 500; // 500 pixels
    private boolean isConnected;
    private GifDrawerListener callbackToActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isConnected = true; // Initialized to true to avoid double loading of chat room initially.
        final View view = inflater.inflate(R.layout.fragment_message_box, container, false);
        mSendButton = view.findViewById(R.id.send_button);
        mMessageEditText = view.findViewById(R.id.message_box);
        gifButton = view.findViewById(R.id.gif_button);
        // TODO: Handle null user/position?
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
                RelativeLayout rl = view.findViewById(R.id.message_box_layout);
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
                    Message outgoingMessage = new Message(User.currentUser(), messageText);
                    chatRoom.addMessage(outgoingMessage);
                    clearMessageBox();
                    hideKeyboard(view);
                } else {
                    Toast.makeText(view.getContext(), "No text in message!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ConnectivityStatusNotifier notifier = ConnectivityStatusNotifier.getInstance();
        notifier.unregisterObserver(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ConnectivityStatusNotifier notifier = ConnectivityStatusNotifier.getInstance();
        notifier.registerObserver(this);
        if (context instanceof Activity) {
            try {
                callbackToActivity = (GifDrawerListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement GifDrawerListener");
            }
        } else {
            Log.d(TAG, "Context in onAttach() is not an activity.");
        }
    }

    private void initGifDrawer() {
        gifDrawer = new GifDrawer(getView(), chatRoom, new GifDrawerAction() {
            @Override
            public void gifSent(Message message) {
                //int lastIndex = mAdapter.insertMessage(message);
                //mAdapter.notifyItemInserted(lastIndex);
                closeGifDrawer();
            }
        });
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
            iconRes = R.drawable.gif_button_enabled;
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
                Drawable newIcon = ContextCompat.getDrawable(MessageBoxFragment.this.getContext(), iconRes);
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
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        AnimatorUtil.on(gifButton).with(new ShrinkingAnimation()).duration(duration).listener(listener).animate();
    }

    private void toggleSendButton() {
        Animation animation;
        if (isGifDrawerOpen) {
            animation = new GrowingAnimation();
        } else {
            animation = new ShrinkingAnimation();
        }
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        AnimatorUtil.on(mSendButton).with(animation).duration(duration).animate();
    }

    private void closeGifDrawer() {
        if (isGifDrawerOpen) {
            toggleGifButton();
            toggleSendButton();
            hideGifDrawer();
            callbackToActivity.onGifDrawerClosed();
        }
    }

    /**
     * Hide the gif drawer itself, and return components to their appropriate states.
     */
    private void hideGifDrawer() {
        mMessageEditText.setHint(R.string.message_box_hint);
        mSendButton.setVisibility(View.VISIBLE);
        isGifDrawerOpen = false;
        gifDrawer.setShouldShow(false);
        // TODO: Actually implement this method: gifDrawer.cancelGifRequests();
        gifDrawer.hide();
        clearMessageBox();
    }

    private void openGifDrawer() {
        if (!isGifDrawerOpen) {
            toggleGifButton();
            toggleSendButton();
            showGifDrawer();
            callbackToActivity.onGifDrawerOpened();
        }
    }

    private void showGifDrawer() {
        clearMessageBox();
        mMessageEditText.setHint(R.string.search_giphy);
        mSendButton.setVisibility(View.GONE);
        isGifDrawerOpen = true;
        gifDrawer.setShouldShow(true);
        gifDrawer.getTrendingGifs();
    }

    private void getGifs(String query) {
        if (query.equals("")) {
            gifDrawer.getTrendingGifs();
        } else {
            gifDrawer.translateTextToGif(query);
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
            updateUI(true);
        }
    }

    private void updateUI(boolean isConnected) {
        int color;
        int hintRes;
        int gifButtonTextColor;
        if (isConnected) {
            color = ContextCompat.getColor(getContext(), android.R.color.transparent);
            hintRes = R.string.message_box_hint;
            gifButtonTextColor = ContextCompat.getColor(getContext(), android.R.color.white);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.NoInternetBackground);
            hintRes = R.string.message_box_disconnected_hint;
            gifButtonTextColor = ContextCompat.getColor(getContext(), R.color.LightBlue);
        }
        View view = getView();
        if (view != null) {
            RelativeLayout layout = getView().findViewById(R.id.message_box_layout);
            layout.setBackgroundColor(color);
            mMessageEditText.setHint(hintRes);
            gifButton.setEnabled(isConnected);
            mSendButton.setEnabled(isConnected);
            gifButton.setTextColor(gifButtonTextColor);
        }
    }

    @Override
    public void onDisconnect(FirebaseError error) {
        if (isConnected) {
            isConnected = false;
            updateUI(false);
        }
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        initGifDrawer();
    }

    interface GifDrawerListener {
        void onGifDrawerOpened();
        void onGifDrawerClosed();
    }
}
