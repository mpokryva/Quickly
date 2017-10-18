package com.android.miki.quickly.user;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.ui.CustomProgressWheel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;

/**
 * Created by mpokr on 9/30/2017.
 */

public class UserProfileActivity extends AppCompatActivity {

    public static final String TAG = UserProfileActivity.class.getName();
    private User user;
    private static final int NAME = 0;
    private static final int AGE = 1;
    private static final int OCCUPATION = 2;
    private static final int EDUCATION = 3;
    private static final int BIO_TITLE = 4;
    private static final int BIO = 5;
    private TextView name;
    private TextView age;
    private TextView bioTitle;
    private TextView bio;
    private TextView occupation;
    private TextView education;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        this.user = (User) getIntent().getSerializableExtra("user");
        final BannerSlider bannerSlider = findViewById(R.id.image_slider);
        final List<Banner> banners = new ArrayList<>();
        fillBannerSlider(bannerSlider, banners, 0);
        initUserFields();
        Toolbar actionBar = findViewById(R.id.action_bar);
        actionBar.setTitle("");
        TextView titleTV = actionBar.findViewById(R.id.toolbar_title);
        titleTV.setText("");
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initUserFields() {
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        bioTitle = findViewById(R.id.bio_title);
        bio = findViewById(R.id.bio);
        occupation = findViewById(R.id.occupation);
        education = findViewById(R.id.education);
        initUserField(NAME);
        initUserField(AGE);
        initUserField(OCCUPATION);
        initUserField(EDUCATION);
        initUserField(BIO_TITLE);
        initUserField(BIO);
    }

    private void initUserField(final int fieldType) {
        final DatabaseReference userRef = DatabaseReferences.USERS
                .child(User.currentUser().getId());
        String childKey = null;
        final TextView view;
        switch (fieldType) {
            case NAME:
                view = name;
                break;
            case AGE:
                view = age;
                childKey = FirebaseRefKeys.AGE;
                break;
            case OCCUPATION:
                view = occupation;
                childKey = FirebaseRefKeys.OCCUPATION;
                break;
            case EDUCATION:
                view = education;
                childKey = FirebaseRefKeys.EDUCATION;
                break;
            case BIO_TITLE:
                view = bioTitle;
                break;
            case BIO:
                view = bio;
                childKey = FirebaseRefKeys.BIO;
                break;
            default:
                view = null;
                childKey = null;
        }
        if (childKey != null) {
            userRef.child(childKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String value = dataSnapshot.getValue(String.class);
                        view.setText(value);
                    } else {
                        view.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (fieldType == NAME) {
            String name = user.getDisplayName() + ", ";
            view.setText(name);
        } else if (fieldType == BIO_TITLE) {
            String bioTitle = "About " + user.getDisplayName();
            view.setText(bioTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void fillBannerSlider(final BannerSlider bannerSlider, final List<Banner> banners, final int i) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        final StorageReference ref = rootRef.child(user.getId()).child(FirebaseRefKeys.IMAGES);
        ref.child("" + i).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                CustomProgressWheel progressWheel = findViewById(R.id.progress_wheel);
                banners.add(new RemoteBanner(uri.toString()).setPlaceHolder(progressWheel.getIndeterminateDrawable()));
                Log.d(TAG, "Got " + i + "th image.");
                fillBannerSlider(bannerSlider, banners, i + 1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CustomProgressWheel progressWheel = findViewById(R.id.progress_wheel);
                progressWheel.setVisibility(View.INVISIBLE);
                bannerSlider.setBanners(banners);
            }
        });
    }

}


