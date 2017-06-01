package com.android.miki.quickly.gif_drawer;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/28/2017.
 */

public class GifDrawer {

    private RecyclerView mRecyclerView;
    private GIFRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final String GIPHY_API_KEY = "dc6zaTOxFJmzC";
    private static final String GIPHY_TRENDING_PATH = "/v1/gifs/trending?";
    private static final String API_KEY_EQUALS = "api_key=";
    private static final String GIPHY_BASE_URL = "http://api.giphy.com";
    private static final String GIPHY_TRANSLATE_PATH = "/v1/gifs/translate?s=";
    private static final String GIPHY_SEARCH_PATH = "/v1/gifs/search?q=";
    private ChatRoom chatRoom;
    private User user;

    public GifDrawer(View view, ChatRoom chatRoom, User user, GifDrawerAction gifDrawerAction) {
        this.chatRoom = chatRoom;
        this.user = user;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.gif_recycler_view);
        mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mAdapter = new GIFRecyclerAdapter(new ArrayList<String>(), chatRoom, user, gifDrawerAction); // No GIF data for now...
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
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
        }).execute(GIPHY_BASE_URL + GIPHY_TRENDING_PATH + API_KEY_EQUALS + GIPHY_API_KEY, "trending");
    }

    public void translateTextToGif(String query) {
        String formattedQuery = "";
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            ch = (ch == ' ') ? '+' : ch;
            formattedQuery += ch;
        }
        new GiphyAPIRequest(new GiphyAPIResponse() {
            @Override
            public void gifURLsRetrieved(List<String> urls) {
                setVisible();
                setGifs(urls);
            }
        }).execute(GIPHY_BASE_URL + GIPHY_SEARCH_PATH + formattedQuery + "&" + API_KEY_EQUALS + GIPHY_API_KEY, query);
    }

}
