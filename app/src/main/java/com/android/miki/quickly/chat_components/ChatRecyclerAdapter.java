package com.android.miki.quickly.chat_components;

/**
 * Created by mpokr on 5/24/2017.
 */

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Gif;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private static final String TAG = "ChatRecyclerAdapter";
    private ActionMode.Callback messageSeletedCallback;
    private ActionMode mActionMode;
    private ChatSelectionActivity mActivity;
    private ArrayList<Boolean> selectedMessages;
    private static final int OUTGOING_TEXT = 0;
    private static final int INCOMING_TEXT = 1;
    private static final int OUTGOING_GIF = 2;
    private static final int INCOMING_GIF = 3;
    private ChatRoom chatRoom;

    public ChatRecyclerAdapter(final ChatRoom chatRoom, final List<Message> messages, final ChatSelectionActivity activity) {
        this.chatRoom = chatRoom;
        this.messages = messages;
        mActivity = activity;
        selectedMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            selectedMessages.add(false); // fill up the array
        }
        messageSeletedCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.message_selected_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.copy_message):
                        for (int i = 0; i < selectedMessages.size(); i++) {
                            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (selectedMessages.get(i)) {
                                Message message = messages.get(i);
                                String content = (message.getMessageText() == null) ? message.getGif().getUrl() : message.getMessageText();
                                ClipData data = ClipData.newPlainText("messageContent", content);
                                clipboard.setPrimaryClip(data);
                                break;
                            }
                        }
                        mActionMode.finish(); // exit out of context action bar to regular action bar
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                for (int i = 0; i < selectedMessages.size(); i++) {
                    selectedMessages.set(i, false);
                }
                notifyDataSetChanged();
                mActionMode = null;
            }
        };
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (messages == null) {
            Log.d("messagesNull", "messages field in ChatRecyclerAdapater is null");
        }
        int layoutRes;

        RecyclerView.ViewHolder vh;
        if (viewType == 0) {
            layoutRes = R.layout.outgoing_text_message; // Outgoing message bubble
        } else if (viewType == 1) {
            layoutRes = R.layout.incoming_text_message; // Incoming message bubble
        } else if (viewType == 2) {
            layoutRes = R.layout.outgoing_gif_message;
        } else {
            layoutRes = R.layout.incoming_gif_message;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        switch (viewType) {
            case OUTGOING_TEXT:
                vh = new OutgoingTextHolder(v, mActivity);
                break;
            case INCOMING_TEXT:
                vh = new IncomingTextHolder(v, mActivity);
                break;
            case OUTGOING_GIF:
                vh = new OutgoingGifHolder(v, mActivity);
                break;
            case INCOMING_GIF:
                vh = new IncomingGifHolder(v, mActivity);
                break;
            default:
                vh = new OutgoingTextHolder(v, mActivity);
                Log.e(TAG, "Unknown view holder type instantiated");
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Message message = messages.get(position);
        if (holder instanceof OutgoingTextHolder) {
            OutgoingTextHolder outgoingTextHolder = (OutgoingTextHolder) holder;
            if (selectedMessages.get(position)) {
                outgoingTextHolder.select();
            } else {
                outgoingTextHolder.deselect();
            }
            outgoingTextHolder.messageText.setText(message.getMessageText());

        } else if (holder instanceof IncomingTextHolder) {
            IncomingTextHolder incomingTextHolder = (IncomingTextHolder) holder;
            if (selectedMessages.get(position)) {
                incomingTextHolder.select();
            } else {
                incomingTextHolder.deselect();
            }
            incomingTextHolder.sender.setText(message.getSender().getDisplayName());
            Integer color = chatRoom.getUserIdColorMap().get(message.getSender().getId());
            incomingTextHolder.sender.setTextColor(color);
            incomingTextHolder.messageText.setText(message.getMessageText());
            incomingTextHolder.messageText.setText(message.getMessageText());
        } else if (holder instanceof OutgoingGifHolder) {
            OutgoingGifHolder outgoingGifHolder = (OutgoingGifHolder) holder;
            if (selectedMessages.get(position)) {
                outgoingGifHolder.select();
            } else {
                outgoingGifHolder.deselect();
            }
            loadGifIntoImageView(outgoingGifHolder.gif, message.getGif(), outgoingGifHolder.progressWheel);

        } else { // IncomingGifHolder
            IncomingGifHolder incomingGifHolder = ((IncomingGifHolder) holder);
            if (selectedMessages.get(position)) {
                incomingGifHolder.select();
            } else {
                incomingGifHolder.deselect();
            }
            loadGifIntoImageView(incomingGifHolder.gif, message.getGif(), incomingGifHolder.progressWheel);

        }
    }


    private void loadGifIntoImageView(ImageView imageView, Gif gif, final ProgressBar progressBar) {
        imageView.setMinimumWidth(gif.getWidth());
        imageView.setMinimumHeight(gif.getHeight());
        Glide.with(imageView.getContext()). load(gif.getUrl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);
    }



    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        try {
            if (message.getSender().equals(User.currentUser())) { // Outgoing messages
                if (message.getMessageText() != null) { // text
                    return OUTGOING_TEXT;
                } else { // GifMessage
                    return OUTGOING_GIF;
                }
            } else { // Incoming message
                if (message.getMessageText() != null) { // text
                    return INCOMING_TEXT;
                } else { // GifMessage
                    return INCOMING_GIF;
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Chatroom id: " + this.chatRoom.getId());
            Log.e(TAG, "Message id: " + message.getId());
            throw  new NullPointerException();
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof OutgoingGifHolder) {
            ImageView imageView = ((OutgoingGifHolder) holder).gif;
            Glide.with(imageView.getContext()).clear(imageView);

        }
    }

    public int insertMessage(Message message) {
        messages.add(message);
        selectedMessages.add(false);
        return messages.size() - 1;
    }

    private void deselectAll() {
        for (int i = 0; i < selectedMessages.size(); i++) {
            if (selectedMessages.get(i)) {
                selectedMessages.set(i, false);
                notifyItemChanged(i);
            }
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    private abstract class MessageViewHolder extends RecyclerView.ViewHolder {


        public MessageViewHolder(final View itemView, final ChatSelectionActivity activity) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mActionMode == null) {
                        mActionMode = activity.startSupportActionMode(messageSeletedCallback);
                    }
                    deselectAll();
                    selectedMessages.set(getAdapterPosition(), true);
                    notifyItemChanged(getAdapterPosition());
                    return true;
                }
            });
        }

        public void select() {
            itemView.setSelected(true);
        }

        public void deselect() {
            itemView.setSelected(false);
        }


    }


    private class IncomingTextHolder extends MessageViewHolder {

        private TextView messageText;
        private TextView sender;

        private IncomingTextHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            sender = (TextView) itemView.findViewById(R.id.sender);
        }

    }


    private class OutgoingTextHolder extends MessageViewHolder {

        private TextView messageText;

        private OutgoingTextHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
        }


    }

    private class OutgoingGifHolder extends MessageViewHolder {

        private ImageView gif;
        private ProgressBar progressWheel;

        private OutgoingGifHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            gif = itemView.findViewById(R.id.gif_image_view);
            progressWheel = itemView.findViewById(R.id.progress_wheel);
        }
    }

    private class IncomingGifHolder extends MessageViewHolder {

        private ImageView gif;
        private ProgressBar progressWheel;

        private IncomingGifHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            gif = (ImageView) itemView.findViewById(R.id.gif_image_view);
            progressWheel = itemView.findViewById(R.id.progress_wheel);
        }
    }

}