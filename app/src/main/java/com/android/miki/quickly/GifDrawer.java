package com.android.miki.quickly;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/28/2017.
 */

public class GifDrawer {

    private RecyclerView mRecyclerView;
    private GIFRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final String GIPHY_TRENDING_URL = "http://api.giphy.com/v1/gifs/trending?api_key=dc6zaTOxFJmzC";

    public GifDrawer(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.gif_recycler_view);
        mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mAdapter = new GIFRecyclerAdapter(null); // No GIF data for now...
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //VerticalSpaceItemDecoration verticalSpaceItemDecoration = new VerticalSpaceItemDecoration(10); // 10dp
        //mRecyclerView.addItemDecoration(verticalSpaceItemDecoration);

    }

    private void setGifs(List<String> gifUrls) {
        mAdapter.setURLs(gifUrls);
        mAdapter.notifyDataSetChanged();
    }

    public void setVisible() {
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void setGone() {
        mRecyclerView.setVisibility(View.GONE);
    }


    public void getTrendingGifs() {
        new GiphyAPIRequest(new GiphyAPIResponse() {
            @Override
            public void gifURLsRetrieved(List<String> urls) {
                setVisible();
                setGifs(urls);
            }
        }).execute(GIPHY_TRENDING_URL);
    }

}
