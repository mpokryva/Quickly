package com.android.miki.quickly.gif_drawer;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.Gif;
import com.android.miki.quickly.models.Message;
import com.android.miki.quickly.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

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
        private ProgressBar mProgressBar;

        private GifViewHolder(View itemView) {
            super(itemView);
            mImageViewGif = (ImageView) itemView.findViewById(R.id.gif_image_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_wheel);
            int lightBlue = ContextCompat.getColor(mImageViewGif.getContext(), R.color.LightBlue);
            mProgressBar.getIndeterminateDrawable().setColorFilter(lightBlue, PorterDuff.Mode.MULTIPLY);
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
//                Glide.with(this.mImageViewGif.getContext()).load(gifURL).into(mImageViewGif);
                loadGifIntoImageView(mImageViewGif, gifURL);
                this.gifURL = gifURL;
            } catch (Exception e) {
                Log.e(TAG, "Failed to set GIF");
                e.printStackTrace();
            }
        }

        private void loadGifIntoImageView(ImageView imageView, String gifURL) {
            // Spinner drawable.
            Glide.with(imageView.getContext()).load(gifURL).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    mProgressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(imageView);
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
