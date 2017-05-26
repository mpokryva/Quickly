package com.android.miki.quickly;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionRecyclerAdapter extends RecyclerView.Adapter<ChatSelectionRecyclerAdapter.ViewHolder> {

    private List<ChatRoom> chatRooms;

    public ChatSelectionRecyclerAdapter(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }

    @Override
    public ChatSelectionRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ChatSelectionRecyclerAdapter.ViewHolder holder, final int position) {
        String chatInfo = "";
        final ChatRoom room = chatRooms.get(position);
        chatInfo += "Creation Timestamp: " + room.getCreationTimestamp() + "\n";
        chatInfo += "Last Message: " + room.getLastMessage().getMessageText() + "\n";
        chatInfo += "Number of participants: " + room.getNumParticipants() + "\n";
        chatInfo += "Chat ID: " + room.getChatId() + "\n";
        holder.mChatInfo.setText(chatInfo);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start chat activity
                Intent chatActivityIntent = new Intent(view.getContext(), ChatActivity.class);
                chatActivityIntent.putExtra("chatRoom", room);
                view.getContext().startActivity(chatActivityIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mChatInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            mChatInfo = (TextView) itemView.findViewById(R.id.chat_info);
        }
    }
}
