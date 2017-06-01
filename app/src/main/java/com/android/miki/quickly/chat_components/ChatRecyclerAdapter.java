package com.android.miki.quickly.chat_components;

/**
 * Created by mpokr on 5/24/2017.
 */

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.bumptech.glide.Glide;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private User user;
    private static final String TAG = "ChatRecyclerAdapter";
    private ActionMode.Callback messageSeletedCallback;
    private ActionMode mActionMode;
    private ChatSelectionActivity mActivity;
    private ChatRoom chatRoom;
    private SparseBooleanArray selectedMessages;

    public ChatRecyclerAdapter(final ChatRoom chatRoom, final List<Message> messages, User user, final ChatSelectionActivity activity) {
        this.chatRoom = chatRoom;
        this.messages = messages;
        this.user = user;
        mActivity = activity;
        selectedMessages = new SparseBooleanArray();
        for (int i = 0; i < messages.size(); i++) {
            selectedMessages.append(i, false); // fill up the array
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
                    case (R.id.delete_message):
                        for (int i = 0; i < selectedMessages.size(); i++) {
                            if (selectedMessages.get(i)) {
                                Message removedMessage = messages.remove(i);
                                chatRoom.removeMessage(removedMessage);
                                notifyItemRemoved(i);
                                notifyItemRangeChanged(i, messages.size());
                                selectedMessages.delete(i);
                            } else {
                                selectedMessages.put(i, false); // deselect message
                            }
                        }
                        mActionMode.finish();
                        Log.d(TAG, "test remove");
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                for (int i = 0; i < selectedMessages.size(); i++) {
                    selectedMessages.put(i, false);
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
            case 0:
                vh = new OutgoingTextHolder(v, mActivity);
                break;
            case 1:
                vh = new IncomingTextHolder(v, mActivity);
                break;
            case 2:
                vh = new OutgoingGifHolder(v, mActivity);
                break;
            case 3:
                vh = new IncomingGifHolder(v, mActivity);
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
            incomingTextHolder.sender.setText(message.getSender().getNickname());
            incomingTextHolder.messageText.setText(message.getMessageText());
            incomingTextHolder.messageText.setText(message.getMessageText());
        } else if (holder instanceof OutgoingGifHolder) {
            OutgoingGifHolder outgoingGifHolder = (OutgoingGifHolder) holder;
            if (selectedMessages.get(position)) {
                outgoingGifHolder.select();
            } else {
                outgoingGifHolder.deselect();
            }
            ImageView imageView = outgoingGifHolder.gif;
            imageView.setMinimumWidth(message.getGif().getWidth());
            imageView.setMinimumHeight(message.getGif().getHeight());
            Glide.with(imageView.getContext()).load(message.getGif().getUrl()).into(imageView);
        } else { // IncomingGifHolder
            IncomingGifHolder incomingGifHolder = ((IncomingGifHolder) holder);
            if (selectedMessages.get(position)) {
                incomingGifHolder.select();
            } else {
                incomingGifHolder.deselect();
            }
            ImageView imageView = incomingGifHolder.gif;
            imageView.setMinimumWidth(message.getGif().getWidth());
            imageView.setMinimumHeight(message.getGif().getHeight());
            Glide.with(imageView.getContext()).load(message.getGif().getUrl()).into(imageView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSender().equals(user)) { // Outgoing messages
            if (message.getMessageText() != null) { // text
                return 0;
            } else { // GifMessage
                return 2;
            }
        } else { // Incoming message
            if (message.getMessageText() != null) { // text
                return 1;
            } else { // GifMessage
                return 3;
            }
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
        return messages.size() - 1;
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
                    if (mActionMode != null) {
                        return false;
                    }
                    mActionMode = activity.startSupportActionMode(messageSeletedCallback);
                    selectedMessages.append(getAdapterPosition(), true);
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

        private OutgoingGifHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            gif = (ImageView) itemView.findViewById(R.id.gif_image_view);
        }
    }

    private class IncomingGifHolder extends MessageViewHolder {

        private ImageView gif;

        private IncomingGifHolder(View itemView, ChatSelectionActivity activity) {
            super(itemView, activity);
            gif = (ImageView) itemView.findViewById(R.id.gif_image_view);
        }
    }


}

