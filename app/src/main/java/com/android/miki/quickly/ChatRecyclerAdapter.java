package com.android.miki.quickly;

/**
 * Created by mpokr on 5/24/2017.
 */

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Message> messages;

    public ChatRecyclerAdapter(List<Message> messages) {
        this.messages = messages;
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
        } else {
            layoutRes = R.layout.incoming_chat_bubble; // Incoming message bubble
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        vh = (viewType == 0) ? new OutgoingViewHolder(v) : new IncomingViewHolder(v);
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
        if (holder instanceof OutgoingViewHolder) {
            OutgoingViewHolder outgoingViewHolder = (OutgoingViewHolder) holder;
            outgoingViewHolder.messageText.setText(message.getMessageText());
            outgoingViewHolder.timeSent.setText(timeString);
        } else {
            IncomingViewHolder incomingViewHolder = (IncomingViewHolder) holder;
            incomingViewHolder.sender.setText(message.getSender());
            incomingViewHolder.messageText.setText(message.getMessageText());
            incomingViewHolder.timeSent.setText(timeString);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals("Miki")) { // Outgoing messages
            return 0;
        } else { // Incoming message
            return 1;
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

    private class IncomingViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView sender;
        private TextView timeSent;

        private IncomingViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            sender = (TextView) itemView.findViewById(R.id.sender);
            timeSent = (TextView) itemView.findViewById(R.id.time_sent);
        }
    }



    private class OutgoingViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView timeSent;

        private OutgoingViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            timeSent = (TextView) itemView.findViewById(R.id.time_sent);
        }
    }




}

