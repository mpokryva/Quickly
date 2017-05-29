package com.android.miki.quickly.gif_drawer;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Gif;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by mpokr on 5/28/2017.
 */

public class GIFRecyclerAdapter extends RecyclerView.Adapter<GIFRecyclerAdapter.GifViewHolder> {


    private List<String> gifURLs;
    private static final String TAG = "GIFRecyclerAdapter";
    private ChatRoom chatRoom;
    private User user;
    private GifDrawerAction gifDrawerAction;

    public GIFRecyclerAdapter(List<String> gifURLs, ChatRoom chatRoom, User user, GifDrawerAction gifDrawerAction) {
        this.gifURLs = gifURLs;
        this.chatRoom = chatRoom;
        this.user = user;
        this.gifDrawerAction = gifDrawerAction;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_gif, parent, false);
        GifViewHolder vh = new GifViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GifViewHolder holder, int position) {
        holder.setGif(gifURLs.get(position));
    }

    @Override
    public int getItemCount() {
        return gifURLs.size();
    }

    public void setURLs(List<String> gifURLs) {
        this.gifURLs = gifURLs;
    }

    @Override
    public void onViewRecycled(GifViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clearGif();
    }


    protected class GifViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageViewGif;
        private String gifURL;

        private GifViewHolder(View itemView) {
            super(itemView);
            mImageViewGif = (ImageView) itemView.findViewById(R.id.gif_image_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int width = mImageViewGif.getWidth();
                    int height = mImageViewGif.getHeight();
                    Message message = new Message(System.currentTimeMillis(), user, new Gif(gifURL, width, height));
                    chatRoom.addMessage(message);
                    gifDrawerAction.gifSent(message);
                }
            });
        }

        private void setGif(String gifURL) {
            try {
                Glide.with(this.mImageViewGif.getContext()).load(gifURL).into(mImageViewGif);
                this.gifURL = gifURL;
            } catch (Exception e) {
                Log.e(TAG, "Failed to set GIF");
                e.printStackTrace();
            }
        }

        private void clearGif() {
            try {
                Glide.with(mImageViewGif.getContext()).clear(mImageViewGif);
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear GIF");
                e.printStackTrace();
            }
        }


    }


}
