package com.android.miki.quickly.group_info;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.chat_components.ChatRoomObserver;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;

import java.util.Iterator;

/**
 * Created by mpokr on 6/17/2017.
 */

public class GroupInfoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ChatRoomObserver {

    private static final String TAG = "GroupInfoRecyclerAdapter";
    private static final int GROUP_NAME_POSITION = 0;
    private static final int USER_NAMES_POSITION = 1;
    private GroupNameDialogListener dialogListener;

    private ChatRoom chatRoom;


    public GroupInfoRecyclerAdapter(ChatRoom chatRoom, GroupNameDialogListener dialogListener) {
        this.chatRoom = chatRoom;
        chatRoom.addObserver(this);
        this.dialogListener = dialogListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case GROUP_NAME_POSITION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_group_name, parent, false);
                return new GroupNameHolder(v);
            case USER_NAMES_POSITION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_group_user_list, parent, false);
                Iterator<User> it = chatRoom.userIterator();
                User currentUser;
                LinearLayout userInfoContainer = v.findViewById(R.id.user_info_container);
                while (it.hasNext()) {
                    currentUser = it.next();
                    RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.user_details,
                            parent, false);
                    TextView name = rl.findViewById(R.id.user_name);
                    TextView university = (TextView) rl.findViewById(R.id.university);
                    name.setText(currentUser.getDisplayName());
                    userInfoContainer.addView(rl);
                }
                return new UserNamesHolder(v);
            default: // Should never be called
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_group_user_list, parent, false);
                return new UserNamesHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == GROUP_NAME_POSITION) {
            GroupNameHolder groupNameHolder = (GroupNameHolder) holder;
            String groupName = (chatRoom.getName() != null) ? chatRoom.getName() : chatRoom.getDefaultName();
            groupNameHolder.groupName.setText(groupName);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private class GroupNameHolder extends RecyclerView.ViewHolder {

        private TextView groupName;
        private ImageButton editGroupNameButton;

        protected GroupNameHolder(final View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.text_group_name);
            editGroupNameButton = itemView.findViewById(R.id.edit_group_name_button);
            editGroupNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogListener.editGroupNameRequested();
                }
            });
        }
    }

    private class UserNamesHolder extends RecyclerView.ViewHolder {

        protected UserNamesHolder(final View itemView) {
            super(itemView);
        }

    }

    @Override
    public void numUsersChanged(int numUsers) {

    }

    @Override
    public void userAdded(User user) {
        // Implement
    }

    @Override
    public void userRemoved(User user) {
        // Implement
    }

    @Override
    public void nameChanged(String name) {
        notifyItemChanged(GROUP_NAME_POSITION);
    }

    @Override
    public void messageAdded(Message message) {

    }
}
