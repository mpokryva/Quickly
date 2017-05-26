package com.android.miki.quickly;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionRecyclerAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private List<ChatRoom> chatRooms;

    public ChatSelectionRecyclerAdapter(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat, parent, false);
        ChatViewHolder vh = new ChatViewHolder(v);
        return vh;
    }

    /*
    This method defines what appears on the window
     */
    @Override
    public void onBindViewHolder(ChatViewHolder holder, final int position) {
        holder.setChatRoom(chatRooms.get(position));
        holder.retrieveMessages();
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }


}
