package com.android.miki.quickly.chat_components;

/**
 * Created by mpokr on 5/24/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.android.miki.quickly.R;
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

    public ChatRecyclerAdapter(List<Message> messages, User user) {
        this.messages = messages;
        this.user = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (messages == null) {
            Log.d("messagesNull", "messages field in ChatRecyclerAdapater is null");
        }
        int layoutRes;

        RecyclerView.ViewHolder vh;
        if (viewType == 0) {
            layoutRes = R.layout.outgoing_chat_bubble; // Outgoing message bubble
        } else if (viewType == 1) {
            layoutRes = R.layout.incoming_chat_bubble; // Incoming message bubble
        } else if (viewType == 2) {
            layoutRes = R.layout.outgoing_gif;
        } else {
            layoutRes = R.layout.incoming_gif;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        switch (viewType) {
            case 0:
                vh = new OutgoingTextHolder(v);
                break;
            case 1:
                vh = new IncomingTextHolder(v);
                break;
            case 2:
                vh = new OutgoingGifHolder(v);
                break;
            case 3:
                vh = new IncomingGifHolder(v);
            default:
                vh = new OutgoingTextHolder(v);
                Log.e(TAG, "Unknown view holder type instantiated");
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Message message = messages.get(position);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(message.getTimestamp());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        String amOrPm = (hours < 12) ? " AM" : " PM";
        if (hours > 12) {
            hours -= 12;
        }
        int minutes = calendar.get(Calendar.MINUTE);
        String timeString = hours + ":" + minutes + amOrPm;
        if (holder instanceof OutgoingTextHolder) {
            OutgoingTextHolder outgoingTextHolder = (OutgoingTextHolder) holder;

            outgoingTextHolder.messageText.setText(message.getMessageText());

        } else if (holder instanceof IncomingTextHolder) {
            IncomingTextHolder incomingTextHolder = (IncomingTextHolder) holder;
            incomingTextHolder.sender.setText(message.getSender().getNickname());
            incomingTextHolder.messageText.setText(message.getMessageText());
            incomingTextHolder.messageText.setText(message.getMessageText());
        } else if (holder instanceof OutgoingGifHolder) {
            ImageView imageView = ((OutgoingGifHolder) holder).gif;
                imageView.setMinimumWidth(message.getGif().getWidth());
            imageView.setMinimumHeight(message.getGif().getHeight());
            Glide.with(imageView.getContext()).load(message.getGif().getUrl()).into(imageView);
        } else { // IncomingGifHolder
            ImageView imageView = ((IncomingGifHolder) holder).gif;
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

    private class IncomingTextHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView sender;

        private IncomingTextHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            sender = (TextView) itemView.findViewById(R.id.sender);
        }
    }


    private class OutgoingTextHolder extends RecyclerView.ViewHolder {

        private TextView messageText;

        private OutgoingTextHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
        }
    }

    private class OutgoingGifHolder extends RecyclerView.ViewHolder {

        private ImageView gif;

        private OutgoingGifHolder(View itemView) {
            super(itemView);
            gif = (ImageView) itemView.findViewById(R.id.gif_image_view);
        }
    }

    private class IncomingGifHolder extends RecyclerView.ViewHolder {

        private ImageView gif;

        private IncomingGifHolder(View itemView) {
            super(itemView);
            gif = (ImageView) itemView.findViewById(R.id.gif_image_view);
        }
    }



}

