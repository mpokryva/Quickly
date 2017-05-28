package com.android.miki.quickly;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by mpokr on 5/28/2017.
 */

public class GIFRecyclerAdapter extends RecyclerView.Adapter<GIFRecyclerAdapter.GifViewHolder> {


    private List<String> gifURLs;
    private static final String TAG = "GIFRecyclerAdapter";

    public GIFRecyclerAdapter(List<String> gifURLs) {
        this.gifURLs = gifURLs;
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

        private GifViewHolder(View itemView) {
            super(itemView);
            mImageViewGif = (ImageView) itemView.findViewById(R.id.gif_image_view);
        }

        private void setGif(String gifURL) {
            try {
                Glide.with(this.mImageViewGif.getContext()).load(gifURL).into(mImageViewGif);
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
