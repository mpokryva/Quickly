package com.android.miki.quickly.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by Miki on 7/18/2017.
 */

public class ImageLoader {

    public ImageLoader() {

    }

    /**
     * Loads image/GIF into an ImageView, displaying a indeterminate progress bar
     * while the image loads.
     * It is the caller's responsibility to make sure the progress bar
     * @param imageUrl The URL to load the image/GIF from.
     * @param imageView The ImageView to load the image into.
     * @param context Any context, will not be retained.
     * @param progressBar A progress bar that displays while the image loads.
     */
    public void loadImageWithSpinner(String imageUrl, ImageView imageView, Context context, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(context).load(imageUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);
    }

}
