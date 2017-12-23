package com.android.miki.quickly.chat_components;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.PersistableBundle;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.miki.quickly.BuildConfig;
import com.android.miki.quickly.R;
import com.android.miki.quickly.core.chat_room.CleanChatRoomJobService;
import com.android.miki.quickly.core.chat_room.GeneralUserRemovalService;
import com.android.miki.quickly.core.network.ConnectivityStatusNotifier;
import com.android.miki.quickly.core.network.ConnectivityStatusObserver;
import com.android.miki.quickly.login_signup.LoginActivity;
import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.user.MyAccountActivity;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.android.miki.quickly.core.chat_room.CleanChatRoomJobService.NUM_USERS_KEY;
import static com.android.miki.quickly.core.chat_room.CleanChatRoomJobService.ROOM_ID_KEY;
import static com.android.miki.quickly.core.chat_room.CleanChatRoomJobService.USER_ID_KEY;

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
    private ViewPager.OnPageChangeListener pageChangeListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        // Set up navigation drawer
        final int MY_ACCOUNT = 1;
        final int LOG_OUT = 2;
        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(MY_ACCOUNT).withName(R.string.my_account).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(LOG_OUT).withName(R.string.log_out).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                        switch ((int) iDrawerItem.getIdentifier()) {
                            case MY_ACCOUNT:
                                Intent myAccountIntent = new Intent(ChatSelectionActivity.this, MyAccountActivity.class);
                                startActivity(myAccountIntent);
                                return true;
                            case LOG_OUT:
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();
                                Intent loginIntent = new Intent(ChatSelectionActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            default:
                                return true;
                        }
                    }
                })
                .withSelectedItem(-1)
                .build();


        // Set up action bar
        actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        // Set up viewpager
        mViewPager = findViewById(R.id.chat_selection_pager);
        mAdapter = new ChatSelectionPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(30);

        messageBoxFragment = (MessageBoxFragment) getSupportFragmentManager()
                .findFragmentById(R.id.message_box_fragment);
        pageChangeListener = new ViewPager.OnPageChangeListener() {
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
                if (isConflictingCleanJob(chatRoom)) {
                    stopCleanJob();
                }
                messageBoxFragment.setChatRoom(chatRoom);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private boolean isConflictingCleanJob(ChatRoom chatRoom) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == User.currentUser().getId().hashCode()) {
                String roomIdInJob = jobInfo.getExtras().getString(ROOM_ID_KEY);
                String userIdInJob = jobInfo.getExtras().getString(USER_ID_KEY);
                return (roomIdInJob != null && userIdInJob != null
                        && roomIdInJob.equals(chatRoom.getId()) && userIdInJob.equals(User.currentUser().getId()));
            }
        }
        return false;
    }

    private void stopCleanJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == User.currentUser().getId().hashCode()) {
                jobScheduler.cancel(jobInfo.getId());
            }
        }
    }

    private void startAutomaticCleaningJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName serviceComponent = new ComponentName(this, GeneralUserRemovalService.class);
        final int GENERAL_USER_REMOVAL_JOB_ID = 1;
        final int PERIODIC_INTERVAL = (BuildConfig.DEBUG) ? 10000 : 120000; // 2 mins. 10 sec when in debug mode.
        JobInfo userRemovalJob = new JobInfo.Builder(GENERAL_USER_REMOVAL_JOB_ID, serviceComponent)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIODIC_INTERVAL)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageChangeListener.onPageSelected(mViewPager.getCurrentItem());
    }

    @Override
    protected void onStop() {
        super.onStop();
        ChatFragment fragInFocus = mAdapter.getCurrentFragment();
        ChatRoom roomInFocus;
        if (fragInFocus != null && (roomInFocus = fragInFocus.getChatRoom()) != null) {
            roomInFocus.removeObserver(fragInFocus);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            final long CLEAN_JOB_DELAY = (BuildConfig.DEBUG) ? 10000 : 60000; // 1 min. 10 sec when in debug mode.
            PersistableBundle extras = new PersistableBundle();
            extras.putString(ROOM_ID_KEY, roomInFocus.getId());
            extras.putString(USER_ID_KEY, User.currentUser().getId());
            extras.putInt(NUM_USERS_KEY, roomInFocus.getNumUsers());
            ComponentName serviceComponent = new ComponentName(this, CleanChatRoomJobService.class);
            JobInfo cleanChatRoomJob = new JobInfo.Builder(User.currentUser().getId().hashCode(), serviceComponent)
                    .setMinimumLatency(CLEAN_JOB_DELAY)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(extras)
                    .setPersisted(true)
                    .build();
            jobScheduler.schedule(cleanChatRoomJob);
        }
    }


    @Override
    public void setTitle(String title) {
        actionBar.setTitle("");
        TextView titleTV = actionBar.findViewById(R.id.toolbar_title);
        titleTV.setText(title);
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
