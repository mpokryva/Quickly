package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mpokr on 5/22/2017.
 */

public class ChatSelectionActivity extends AppCompatActivity implements ActionBarListener, MessageBoxFragment.GifDrawerListener {

    private CustomViewPager mViewPager;
    private ChatSelectionPagerAdapter mAdapter;
    private Toolbar actionBar;
    private MessageBoxFragment messageBoxFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        mViewPager = findViewById(R.id.chat_selection_pager);
        mAdapter = new ChatSelectionPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(30);
        messageBoxFragment = (MessageBoxFragment) getSupportFragmentManager().findFragmentById(R.id.message_box_fragment);
        final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                messageBoxFragment.revertToStartState();
                loadRoom(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mViewPager.addOnPageChangeListener(pageChangeListener);
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(mViewPager.getCurrentItem());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                loadRoom(mViewPager.getCurrentItem());
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRoom(int position) {
        mAdapter.loadRoom(mViewPager, position, new FirebaseListener<ChatRoom>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onError(FirebaseError error) {

            }

            @Override
            public void onSuccess(ChatRoom chatRoom) {
                messageBoxFragment.setChatRoom(chatRoom);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setTitle(String title) {
        actionBar.setTitle(title);
    }

    @Override
    public void onGifDrawerOpened() {
        ChatFragment currentFragment = mAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.onGifDrawerOpened();
        } else {
            Toast.makeText(this, R.string.something_weird_happened, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGifDrawerClosed() {
        ChatFragment currentFragment = mAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.onGifDrawerClosed();
        } else {
            Toast.makeText(this, R.string.something_weird_happened, Toast.LENGTH_SHORT).show();
        }
    }
}
