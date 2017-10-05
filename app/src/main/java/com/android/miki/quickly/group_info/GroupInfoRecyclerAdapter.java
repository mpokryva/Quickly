package com.android.miki.quickly.group_info;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
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
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.ui.CustomProgressWheel;
import com.android.miki.quickly.user.UserProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Iterator;

/**
 * Created by mpokr on 6/17/2017.
 */

public class GroupInfoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ChatRoomObserver {

    private static final String TAG = "GroupInfoRecyclerAdapter";
    private static final int GROUP_NAME_POSITION = 0;
    private static final int USER_NAMES_POSITION = 1;
    private GroupInfoActivity activity;
    private GroupNameDialogListener dialogListener;
    private ChatRoom chatRoom;
    private final int MAX_STORAGE_RETRY_MILLISECONDS = 4000;


    public GroupInfoRecyclerAdapter(GroupInfoActivity activity, ChatRoom chatRoom, GroupNameDialogListener dialogListener) {
        this.activity = activity;
        this.chatRoom = chatRoom;
        this.dialogListener = dialogListener;
        chatRoom.addObserver(this);
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(MAX_STORAGE_RETRY_MILLISECONDS);
        FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(MAX_STORAGE_RETRY_MILLISECONDS);
        FirebaseStorage.getInstance().setMaxOperationRetryTimeMillis(MAX_STORAGE_RETRY_MILLISECONDS);
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
                    if (currentUser != null) {
                        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.user_details,
                                parent, false);
                        setUserDetails(rl, currentUser);
                        userInfoContainer.addView(rl);
                    }
                }
                return new UserNamesHolder(v);
            default: // Should never be called
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_group_user_list, parent, false);
                return new UserNamesHolder(v);
        }
    }

    private void setUserDetails(RelativeLayout userDetailsLayout, User user) {
        TextView name = userDetailsLayout.findViewById(R.id.user_name);
        RoundedImageView userPhotoImageView = userDetailsLayout.findViewById(R.id.user_photo);
        CustomProgressWheel progressWheel = userDetailsLayout.findViewById(R.id.progress_wheel);
        TextView noPhotoView = userDetailsLayout.findViewById(R.id.no_user_photo_textview);
        setImageFromFromStorage(userPhotoImageView, noPhotoView, progressWheel, user);
        name.setText(user.getDisplayName());
    }

    private void setImageFromFromStorage(final RoundedImageView imageView, final TextView noPhotoView, final CustomProgressWheel progressWheel, final User user) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference ref = rootRef.child(user.getId()).child(FirebaseRefKeys.IMAGES).child("" + 0);
        ref.getDownloadUrl().addOnCompleteListener(activity, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    Glide.with(GroupInfoRecyclerAdapter.this.activity).load(uri).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            configureNoPhotoView(noPhotoView, user);
                            noPhotoView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                            progressWheel.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                       DataSource dataSource, boolean isFirstResource) {
                            noPhotoView.setVisibility(View.GONE);
                            progressWheel.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(GroupInfoRecyclerAdapter.this.activity, UserProfileActivity.class);
                                    i.putExtra("user", user);
                                    GroupInfoRecyclerAdapter.this.activity.startActivity(i);
                                }
                            });
                            return false;
                        }
                    }).into(imageView);
                } else {
                    configureNoPhotoView(noPhotoView, user);
                    noPhotoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    progressWheel.setVisibility(View.GONE);
                }
            }
        });
    }

    private void configureNoPhotoView(TextView noPhotoView, User user) {
        String firstLetter = Character.toString(Character.toUpperCase(user.getDisplayName().charAt(0)));
        noPhotoView.setText(firstLetter);
        int color = chatRoom.getUserIdColorMap().get(user.getId());
        float lighteningRatio = 0.4f;
        int lightenedColor = ColorUtils.blendARGB(color, Color.WHITE, lighteningRatio);
        noPhotoView.setTextColor(lightenedColor);
        Drawable tintedDrawable = noPhotoView.getBackground().mutate();
        tintedDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        noPhotoView.setBackground(tintedDrawable);
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
