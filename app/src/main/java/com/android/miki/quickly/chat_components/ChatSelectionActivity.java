package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.user.MyAccountActivity;
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
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private static final String TAG = ChatSelectionActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        // Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open_drawer, R.string.close_drawer) {

        };
        drawerLayout.addDrawerListener(drawerToggle);
        NavigationView navigationView = findViewById(R.id.drawer_navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.my_account:
                        Intent i = new Intent(ChatSelectionActivity.this, MyAccountActivity.class);
                        startActivity(i);
                        return true;
                    default:
                        return true;
                }
            }
        });


        // Set up action bar
        actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        // Set up viewpager
        mViewPager = findViewById(R.id.chat_selection_pager);
        mAdapter = new ChatSelectionPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(30);

        messageBoxFragment = (MessageBoxFragment) getSupportFragmentManager()
                .findFragmentById(R.id.message_box_fragment);
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
        actionBar.setTitle("");
        TextView titleTV = actionBar.findViewById(R.id.toolbar_title);
        titleTV.setText(title);
//        int i = 0;
//        while (titleTV.getLineCount() > 1) {
//            String prevTitle = titleTV.getText().toString();
//            int index = prevTitle.lastIndexOf(',');
//            if (index >= 0) {
//                i++;
//                String newTitle = prevTitle.substring(0, index) + " + " + i;
//                titleTV.setText(newTitle);
//            }
//        }
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
