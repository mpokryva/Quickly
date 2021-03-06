package com.android.miki.quickly.gif_drawer;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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

    public GIFRecyclerAdapter(List<String> gifURLs, ChatRoom chatRoom, GifDrawerAction gifDrawerAction) {
        this.gifURLs = gifURLs;
        this.chatRoom = chatRoom;
        this.gifDrawerAction = gifDrawerAction;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_drawer_item, parent, false);
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
        private ProgressBar progressWheel;

        private GifViewHolder(View itemView) {
            super(itemView);
            mImageViewGif = itemView.findViewById(R.id.gif_image_view);
            progressWheel = itemView.findViewById(R.id.progress_wheel);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int width = mImageViewGif.getWidth();
                    int height = mImageViewGif.getHeight();
                    Message message = new Message(User.currentUser(), new Gif(gifURL, width, height));
                    chatRoom.addMessage(message);
                    gifDrawerAction.gifSent(message);
                }
            });
        }

        private void setGif(String gifURL) {
            try {
                loadGifIntoImageView(mImageViewGif, gifURL);
                this.gifURL = gifURL;
            } catch (Exception e) {
                Log.e(TAG, "Failed to set GIF");
                e.printStackTrace();
            }
        }

        private void loadGifIntoImageView(final ImageView imageView, String gifURL) {
            Glide.with(imageView.getContext()).load(gifURL)
                    .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressWheel.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressWheel.setVisibility(View.GONE);
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
